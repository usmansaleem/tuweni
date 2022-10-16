/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tuweni.scuttlebutt.lib

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.tuweni.concurrent.AsyncCompletion
import org.apache.tuweni.concurrent.AsyncResult
import org.apache.tuweni.scuttlebutt.Invite
import org.apache.tuweni.scuttlebutt.MalformedInviteCodeException
import org.apache.tuweni.scuttlebutt.lib.model.Peer
import org.apache.tuweni.scuttlebutt.lib.model.PeerStateChange
import org.apache.tuweni.scuttlebutt.lib.model.StreamHandler
import org.apache.tuweni.scuttlebutt.rpc.RPCAsyncRequest
import org.apache.tuweni.scuttlebutt.rpc.RPCFunction
import org.apache.tuweni.scuttlebutt.rpc.RPCResponse
import org.apache.tuweni.scuttlebutt.rpc.RPCStreamRequest
import org.apache.tuweni.scuttlebutt.rpc.mux.Multiplexer
import org.apache.tuweni.scuttlebutt.rpc.mux.ScuttlebuttStreamHandler
import org.apache.tuweni.scuttlebutt.rpc.mux.exceptions.ConnectionClosedException
import java.io.IOException
import java.util.function.Function
import java.util.stream.Collectors

/**
 * A service for operations that connect nodes together and other network related operations
 *
 *
 * Assumes the standard 'ssb-gossip' plugin is installed and enabled on the node that we're connected to (or that RPC
 * functions meeting its manifest's contract are available.).
 *
 *
 * Should not be constructed directly, should be used via an ScuttlebuttClient instance.
 */
class NetworkService(private val multiplexer: Multiplexer) {
  companion object {
    // We don't represent all the fields returned over RPC in our java classes, so we configure the mapper
    // to ignore JSON fields without a corresponding Java field
    private val mapper: ObjectMapper =
      ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  /**
   * Generates an invite code which can be used by another node to connect to our node, and have each node will befriend
   * each other.
   *
   * @param validForUses the number of times this should be usable by different people (0 if an infinite number of
   * times.)
   * @return the invite that was generated.
   */
  fun generateInviteCode(validForUses: Int): AsyncResult<Invite> {
    val function = RPCFunction(listOf("invite"), "create")
    val request = RPCAsyncRequest(function, listOf<Any>(validForUses))
    try {
      return multiplexer.makeAsyncRequest(request).then { response: RPCResponse ->
        try {
          return@then AsyncResult.completed(inviteFromRPCResponse(response))
        } catch (malformedInviteCodeException: MalformedInviteCodeException) {
          return@then AsyncResult.exceptional<Invite>(malformedInviteCodeException)
        }
      }
    } catch (ex: JsonProcessingException) {
      return AsyncResult.exceptional(ex)
    }
  }

  /**
   * Redeems an invite issued by another node. If successful, the node will connect to the other node and each node will
   * befriend each other.
   *
   * @param invite the invite to redeem
   * @return a completion handle
   */
  fun redeemInviteCode(invite: Invite): AsyncCompletion {
    val function = RPCFunction(listOf("invite"), "accept")
    val request = RPCAsyncRequest(function, listOf<Any>(invite.toCanonicalForm()))
    return try {
      multiplexer.makeAsyncRequest(request).thenAccept { }
    } catch (ex: JsonProcessingException) {
      AsyncCompletion.exceptional(ex)
    }
  }

  /**
   * Queries for the list of peers the instance is connected to.
   *
   * @return a handle to seeing all peers
   */
  val connectedPeers: AsyncResult<List<Peer>>
    get() = allKnownPeers
      .thenApply { peers: List<Peer> ->
        peers.stream().filter { peer: Peer -> (peer.state == "connected") }
          .collect(Collectors.toList())
      }

  /**
   * Queries for all the peers the instance is aware of in its gossip table.
   *
   * @return a handle to the list of all known peers
   */
  val allKnownPeers: AsyncResult<List<Peer>>
    get() {
      val function = RPCFunction(listOf("gossip"), "peers")
      val request = RPCAsyncRequest(function, listOf())
      try {
        return multiplexer.makeAsyncRequest(request).then { rpcResponse: RPCResponse ->
          try {
            val peers: List<Peer> =
              rpcResponse.asJSON(
                mapper,
                object :
                  TypeReference<List<Peer>>() {}
              )
            return@then AsyncResult.completed(
              peers
            )
          } catch (e: IOException) {
            return@then AsyncResult.exceptional<List<Peer>>(
              e
            )
          }
        }
      } catch (e: JsonProcessingException) {
        return AsyncResult.exceptional(e)
      }
    }

  /**
   * Opens a stream of peer connection state changes.
   *
   * @param streamHandler A function that can be invoked to instantiate a stream handler to pass events to, with a given
   * runnable to close the stream early
   * @throws JsonProcessingException If the stream could be opened due to an error serializing the request
   * @throws ConnectionClosedException if the stream could not be opened due to the connection being closed
   */
  @Throws(JsonProcessingException::class, ConnectionClosedException::class)
  fun createChangesStream(streamHandler: Function<Runnable?, StreamHandler<PeerStateChange?>>) {
    val function = RPCFunction(listOf("gossip"), "changes")
    val request = RPCStreamRequest(function, listOf())
    multiplexer.openStream(
      request
    ) { closer: Runnable ->
      object : ScuttlebuttStreamHandler {
        var changeStream: StreamHandler<PeerStateChange?> =
          streamHandler.apply(closer)

        override fun onMessage(message: RPCResponse) {
          try {
            val peerStateChange: PeerStateChange =
              message.asJSON(mapper, PeerStateChange::class.java)
            changeStream.onMessage(peerStateChange)
          } catch (e: IOException) {
            changeStream.onStreamError(e)
            closer.run()
          }
        }

        override fun onStreamEnd() {
          changeStream.onStreamEnd()
        }

        override fun onStreamError(ex: Exception) {
          changeStream.onStreamError(ex)
        }
      }
    }
  }

  @Throws(MalformedInviteCodeException::class)
  private fun inviteFromRPCResponse(response: RPCResponse): Invite {
    val rawInviteCode: String = response.asString()
    return Invite.fromCanonicalForm(rawInviteCode)
  }
}
