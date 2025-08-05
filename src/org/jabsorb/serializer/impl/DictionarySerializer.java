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

package org.jabsorb.serializer.impl;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.MarshallingModeContext;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.JSONSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises Hashtables
 * 
 * TODO: why not use a map serialiser?
 */
public class DictionarySerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { Hashtable.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Dictionary.class
        .isAssignableFrom(clazz)));
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Object marshall(SerializerState state, Object p, Object o)
      throws MarshallException
  {
    Dictionary ht = (Dictionary) o;
    JSONObject obj = null;
    JSONObject mapdata = new JSONObject();

    String parentNode = null;
    try
    {
      switch (MarshallingModeContext.get()) {
        case JABSORB:
          obj = new JSONObject();
          if (ser.getMarshallClassHints())
          {
            obj.put("javaClass", o.getClass().getName());
          }
          parentNode = "map";
          obj.put(parentNode, mapdata);
          break;
        case STANDARD_REST:
          // obj becomes the actual map
          obj = mapdata;
          break;
        default:
          throw new MarshallException("Invalid marshall mode: " + MarshallingModeContext.get());
      }
      state.push(o, mapdata, parentNode);
    }
    catch (JSONException e)
    {
      throw (MarshallException) new MarshallException("Could not put data"+ e.getMessage()).initCause(e);
    }
    Object key = null;

    try
    {
      Enumeration en = ht.keys();
      while (en.hasMoreElements())
      {
        key = en.nextElement();
        String keyString = key.toString();  // only support String keys

        Object json = ser.marshall(state, mapdata, ht.get(key), keyString);

        // omit the object entirely if it's a circular reference or duplicate
        // it will be regenerated in the fixups phase
        if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json)
        {
          mapdata.put(keyString,json );
        }

      }
    }
    catch (MarshallException e)
    {
      throw (MarshallException) new MarshallException("map key " + key + " " + e.getMessage()).initCause(e);
    }
    catch (JSONException e)
    {
      throw (MarshallException) new MarshallException("map key " + key + " " + e.getMessage()).initCause(e);
    }
    finally
    {
      // pop can only be done if state.push() was done against a parent ref
      if (parentNode != null) {
        state.pop();
      }
    }
    return obj;
  }

  // TODO: cache the result somehow so that an unmarshall
  // following a tryUnmarshall doesn't do the same work twice!
  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    sanityCheckForDictionaryClass(getJavaClass(jso, clazz));

    JSONObject jsonmap = getJsonObjectByMarshallingMode(jso, "map");

    ObjectMatch m = new ObjectMatch(-1);
    state.setSerialized(o, m);

    Iterator i = jsonmap.keys();
    String key = null;
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        m.setMismatch(ser.tryUnmarshall(state, null, jsonmap.get(key)).max(m).getMismatch());
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }

    return m;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    sanityCheckForDictionaryClass(getJavaClass(jso, clazz));

    JSONObject jsonmap = getJsonObjectByMarshallingMode(jso, "map");

    if (jsonmap == null)
    {
      throw new UnmarshallException("map missing");
    }

    Hashtable ht = new Hashtable();
    state.setSerialized(o, ht);

    Iterator i = jsonmap.keys();
    String key = null;
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        ht.put(key, ser.unmarshall(state, null, jsonmap.get(key)));
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }
    return ht;
  }

  /**
   * Check if java_class is either Dictionary or Hashtable
   * Throws UnmarshallException otherwise
   * @param java_class
   * @throws UnmarshallException
   */
  private static void sanityCheckForDictionaryClass(String java_class) throws UnmarshallException {
    if (!(java_class.equals("java.util.Dictionary") || java_class
            .equals("java.util.Hashtable")))
    {
      throw new UnmarshallException("not a Dictionary");
    }
  }
}
