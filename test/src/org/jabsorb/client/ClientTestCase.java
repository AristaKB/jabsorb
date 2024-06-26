/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Client, a Java client extension to JSON-RPC-Java
 * (C) Copyright CodeBistro 2007, Sasha Ovsankin <sasha at codebistro dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jabsorb.client;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jabsorb.test.BeanA;
import org.jabsorb.test.BeanB;
import org.jabsorb.test.ITest;
import org.jabsorb.test.Test;

/**
 * This test implements some of Jabsorb tests.
 */
public class ClientTestCase extends ServerTestBase
{

  HttpState         state;

  TransportRegistry registry;
  
  public ClientTestCase() {
	  
  }

  protected void setUp() throws Exception
  {
    super.setUp(); // Makes sure jabsorb server tests are running at this URL

    registry = new TransportRegistry();
  }

  TransportRegistry getRegistry()
  {
    if (registry == null)
      registry = new TransportRegistry(); // Standard registry by default
    return registry;
  }

  /**
   * JSON-RPC tests need this setup to operate propely. This call invokes
   * registerObject("test", ...) from the JSP
   * @deprecated since we are running the server in-process
   */
  void setupServerTestEnvironment(String url) throws HttpException, IOException
  {
    HttpClient client = new HttpClient();
    state = new HttpState();
    client.setState(state);
    GetMethod method = new GetMethod(url);
    int status = client.executeMethod(method);
    if (status != HttpStatus.SC_OK)
      throw new RuntimeException(
          "Setup did not succeed. Make sure the JSON-RPC-Java test application is running on "
              + getServiceRootURL());
  }

  /**
   * Test for invalid URL
   */
  public void testBadClient()
  {
    Client badClient = new Client(registry
        .createSession("http://non-existing-server:99"));
    try
    {
      ITest badTest = (ITest) badClient.openProxy("test", ITest.class);
      badTest.voidFunction();
      fail();
    }
    catch (ClientError err)
    {
      // Cool, we got error!
    }
  }

  public void testStandardSession()
  {
    Client client = new Client(getRegistry().createSession(
        getServiceRootURL() + "/JSON-RPC"));
    ITest test = (ITest) client.openProxy("test", ITest.class);
    basicClientTest(test);
  }
  
  HTTPSession newHTTPSession(String url) {
	  try {
		  TransportRegistry reg= getRegistry();
		  // Note: HTTPSession is not registered by default. Normally you would
		  // register during initialization. In this test, we are testing different
		  // states of the registry, hence we register it here and clean up afterwards
		  HTTPSession.register(reg);		
		  // Note: will not work without registering HTTPSession, see #setUp() 
	      return (HTTPSession) getRegistry().createSession(url);
	  }
      finally
      {
        // Modified the registry; let's clean up after ourselves. Next call
    	// to getRegistry will create a new one
        registry = null;
      }
  }
  
  public void testHTTPSession()
  {
      Client client = new Client(newHTTPSession(getServiceURL()));
      ITest test = (ITest) client.openProxy("test", ITest.class);
      basicClientTest(test);
  }
  
  void basicClientTest(ITest test)
  {
    test.voidFunction();
    assertEquals("hello", test.echo("hello"));
    assertEquals(1234, test.echo(1234));
    int[] ints = { 1, 2, 3 };
    assertTrue(Arrays.equals(ints, test.echo(ints)));
    String[] strs = { "foo", "bar", "baz" };
    assertTrue(Arrays.equals(strs, test.echo(strs)));
    Test.Wiggle wiggle = new Test.Wiggle();
    assertEquals(wiggle.toString(), test.echo(wiggle).toString());
    Test.Waggle waggle = new Test.Waggle(1);
    assertEquals(waggle.toString(), test.echo(waggle).toString());
    assertEquals('?', test.echoChar('?'));
    Integer into = new Integer(1234567890);
    assertEquals(into, test.echoIntegerObject(into));
    Long longo = new Long(1099511627776L);
    assertEquals(longo, test.echoLongObject(longo));
    Float floato = new Float(3.3F);
    assertEquals(floato, test.echoFloatObject(floato));
    Double doublo = new Double(3.1415926F);
    assertEquals(doublo, test.echoDoubleObject(doublo));
   
    /*
		var circRefList =
		{
		  "list": [
		    0,
		    1,
		    2,
		    {
		      "javaClass": "org.jabsorb.test.BeanB",
		      "beanA": {
		        "javaClass": "org.jabsorb.test.BeanA",
		        "id": 0
		      },
		      "id": 0
		    },
		    {
		      "javaClass": "java.util.HashMap",
		      "map": {
		        "2": "two",
		        "0": "zero",
		        "1": "one"
		      }
		    }
		  ],
		  "javaClass": "java.util.ArrayList"
		};
		
		circRefList.list[3].beanA.beanB = circRefList.list[3];
		circRefList.list[4].map.buckle_my_shoe = circRefList;
		circRefList.list[4].map.aBean = circRefList.list[3].beanA;
     */

    ArrayList circRefList= new ArrayList();
    circRefList.add(new Integer(0));
    circRefList.add(new Integer(1));
    circRefList.add(new Integer(2));
    BeanA beanA= new BeanA();
    BeanB beanB= new BeanB();
    beanB.setBeanA(beanA);
    circRefList.add(beanB);
    HashMap map= new HashMap();
    map.put("2", "two");
    map.put("0", "zero");
    map.put("1", "one");
    circRefList.add(map);
    //circular ref
    beanA.setBeanB(beanB);
    map.put("buckle_my_shoe", circRefList);
    map.put("aBean", beanA);
    
    ArrayList returnedCircRefList= (ArrayList) test.echoObject(circRefList);
    // FIXME circular references are not being transferred here
    // assertEquals(circRefList, returnedCircRefList);
    
  }

  // TODO run embedded proxy server (is  Jetty capable of working like a proxy?) to really test proxy.
  // Right now, we are just testing that the proxy parameters are being set
  public void testProxyConfiguration() {
	  HTTPSession proxiedSession= newHTTPSession(getServiceURL());
	  int proxyPort= 40888;	// hopefully, the port is unused
	  proxiedSession.getHostConfiguration().setProxy("localhost", proxyPort);
	  Client client = new Client(proxiedSession);
	  ITest proxyObject= (ITest)client.openProxy("test", ITest.class);
	  try {
		  proxyObject.voidFunction();
	  } catch(ClientError ex) {
		  if ( !(ex.getCause() instanceof ConnectException) )
			  fail("expected ConnectException, got " + ex.getCause().getClass().getName());
	  }
  }


  String getServiceURL() {
	  return getServiceRootURL() + "/JSON-RPC";
  }
}
