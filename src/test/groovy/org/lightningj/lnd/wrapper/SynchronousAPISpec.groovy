/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public License   *
 *  (LGPL-3.0-or-later)                                                  *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.lnd.wrapper

import io.grpc.ManagedChannel
import org.lightningj.lnd.proto.LightningApi
import org.lightningj.lnd.wrapper.message.WalletBalanceResponse
import spock.lang.Specification

import java.util.logging.Logger

/**
 * Unit tests for SynchronousAPI methods.
 *
 * Created by Philip Vendil.
 */
class SynchronousAPISpec extends Specification {

    SynchronousLndAPI api = new SynchronousLndAPI(Mock(ManagedChannel))

    def setup(){
        api.log = Mock(Logger)
    }

    def "SynchronousAPI initializes constructors properly."(){
        setup:
        File macaroonPath = new File(this.getClass().getResource("/admin.macaroon").path)
        when: // This constructor
        SynchronousLndAPI api1 = new SynchronousLndAPI("localhost",8080,new File("src/test/resources/cert.pem"),macaroonPath)
        then:
        api1.channel != null
    }

    def "Verify that processResponse performs validation and debug logging"(){
        setup:
        Message m = Mock(Message)
        when:
        api.processResponse(m)
        then:
        1 * m.validate() >> { APISpec.getValidValidationResult()}

        when:
        WalletBalanceResponse resp = new WalletBalanceResponse()
        WalletBalanceResponse resp2 = api.processResponse(resp)
        then:
        resp == resp2
        1 * api.log.fine({ it =~'Received response message: WalletBalanceResponse: '})
    }


    def "Verify that processRepeatableResponse performs validation and debug logging on each message in iterator."(){
        when:
        Iterator result = api.processRepeatableResponse([genWalletBalanceResponseApi(3213L),genWalletBalanceResponseApi(4213L)].iterator())
        WalletBalanceResponse r1 = result.next()
        WalletBalanceResponse r2 = result.next()
        then:
        !result.hasNext()
        r1.totalBalance == 3213L
        r2.totalBalance == 4213L
        1 * api.log.fine( {it =~'Received response message: WalletBalanceResponse: '})
        1 * api.log.fine( { it =~ 'Received response message: WalletBalanceResponse: '})
    }

    private LightningApi.WalletBalanceResponse genWalletBalanceResponseApi(long totalValue){
        LightningApi.WalletBalanceResponseOrBuilder b = LightningApi.WalletBalanceResponse.newBuilder()
        b.setTotalBalance(totalValue)
        b.build()
    }
}
