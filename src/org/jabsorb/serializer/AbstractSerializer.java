/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
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

package org.jabsorb.serializer;

import org.jabsorb.JSONSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Convenience class for implementing Serializers providing default setOwner and
 * canSerialize implementations.
 */
public abstract class AbstractSerializer implements Serializer
{

  /**
   * Main serialiser
   */
  protected JSONSerializer ser;

  /**
   * Default check that simply tests the given serializeable class arrays to
   * determine if the pair of classes can be serialized/deserialized from this
   * Serializer.
   * 
   * @param clazz Java type to check if this Serializer can handle.
   * @param jsonClazz JSON type to check this Serializer can handle.
   * 
   * @return true If this Serializer can serialize/deserialize the given
   *         java,json pair.
   */
  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    boolean canJava = false, canJSON = false;

    Class serializableClasses[] = getSerializableClasses();
    for (int i = 0; i < serializableClasses.length; i++)
    {
      if (clazz == serializableClasses[i])
      {
        canJava = true;
      }
    }

    if (jsonClazz == null)
    {
      canJSON = true;
    }
    else
    {
      Class jsonClasses[] = getJSONClasses();
      for (int i = 0; i < jsonClasses.length; i++)
      {
        if (jsonClazz == jsonClasses[i])
        {
          canJSON = true;
        }
      }
    }

    return (canJava && canJSON);
  }

  /**
   * Set the JSONSerialiser that spawned this object.
   * 
   * @param ser The parent serialiser.
   */
  public void setOwner(JSONSerializer ser)
  {
    this.ser = ser;
  }

  /**
   * Sets javaClass attribute to JSONObject. javaClass value is retrieved from class name of input Object instance
   * @param o
   * @param obj
   * @throws MarshallException
   */
  protected void setJavaClassToJSONObject(Object o, JSONObject obj) throws MarshallException {
    if (ser.getMarshallClassHints())
    {
      try
      {
        obj.put("javaClass", o.getClass().getName());
      }
      catch (JSONException e)
      {
        throw new MarshallException("javaClass not found!");
      }
    }
  }

  /**
   * Get javaClass from the input object's hint. If unable to get javaClass from the hint, use input clazz
   * Throws UnmarshallException if unable to retrieve javaClass
   * @param jso
   * @return
   * @throws UnmarshallException
   */
  protected static String getJavaClass(JSONObject jso, Class clazz) throws UnmarshallException {
    String javaClass = null;
    try
    {
      javaClass = jso.getString("javaClass");
    }
    catch (JSONException e)
    {
      // try getting javaClass using clazz
    }
    if (clazz != null) {
      javaClass = clazz.getName();
    }

    if (javaClass == null)
    {
      throw new UnmarshallException("no type hint");
    }
    return javaClass;
  }


  /**
   * Returns JSON object as per the MarshallingMode.
   * JABSORB input obj -> '{objKey: {jso}, javaClass: className}'
   * STANDARD_REST input obj -> '{jso}' (i.e. return as is jso object)
   * @param jso
   * @return
   * @throws UnmarshallException
   */
  protected static JSONObject getJsonObjectByMarshallingMode(JSONObject jso, String objKey) throws UnmarshallException {
    JSONObject jsonObj;
    switch (MarshallingModeContext.get()) {
      case JABSORB:
        try
        {
          jsonObj = jso.getJSONObject(objKey);
        }
        catch (JSONException e)
        {
          throw new UnmarshallException(objKey + " missing", e);
        }
        break;
      case STANDARD_REST:
        // jso is the actual object
        jsonObj = jso;
        break;
      default:
        throw new UnmarshallException("Invalid unarshall mode: " + MarshallingModeContext.get());
    }

    if (jsonObj == null)
    {
      throw new UnmarshallException(objKey + " missing");
    }
    return jsonObj;
  }

}
