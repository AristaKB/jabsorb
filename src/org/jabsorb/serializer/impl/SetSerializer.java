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

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.MarshallingModeContext;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.JSONSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises Sets
 * 
 * TODO: if this serialises a superclass does it need to also specify the
 * subclasses?
 */
public class SetSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { Set.class,
      HashSet.class, TreeSet.class, LinkedHashSet.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Set.class
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
    Set set = (Set) o;

    JSONObject obj = null;
    JSONObject setdata = new JSONObject();
    String parentNode = null;
    try
    {
      switch (MarshallingModeContext.get()) {
        case JABSORB:
          obj = new JSONObject();
          if (ser.getMarshallClassHints())
          {
            try
            {
              obj.put("javaClass", o.getClass().getName());
            }
            catch (JSONException e)
            {
              throw new MarshallException("javaClass not found!", e);
            }
          }
          parentNode = "set";
          obj.put(parentNode, setdata);
          break;
        case STANDARD_REST:
          // obj becomes the actual set
          obj = setdata;
          break;
        default:
          throw new MarshallException("Invalid marshall mode: " + MarshallingModeContext.get());
      }

      state.push(o, setdata, parentNode);
    }
    catch (JSONException e)
    {
      throw new MarshallException("Could not set 'set': " + e.getMessage(), e);
    }
    Object key = null;
    Iterator i = set.iterator();

    try
    {
      while (i.hasNext())
      {
        key = i.next();
        String keyString = key.toString();  // only support String keys
        Object json = ser.marshall(state, setdata, key, keyString);

        // omit the object entirely if it's a circular reference or duplicate
        // it will be regenerated in the fixups phase
        if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json)
        {
          setdata.put(keyString, json);
        }
      }
    }
    catch (MarshallException e)
    {
      throw new MarshallException("set key " + key + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new MarshallException("set key " + key + e.getMessage(), e);
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

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class = getJavaClass(jso, clazz);
    
    if (!(java_class.equals("java.util.Set")
        || java_class.equals("java.util.AbstractSet")
        || java_class.equals("java.util.LinkedHashSet")
        || java_class.equals("java.util.TreeSet") || java_class
        .equals("java.util.HashSet")))
    {
      throw new UnmarshallException("not a Set");
    }

    JSONObject jsonset = getJsonObjectByMarshallingMode(jso, "set");

    ObjectMatch m = new ObjectMatch(-1);
    state.setSerialized(o, m);
    Iterator i = jsonset.keys();
    String key = null;

    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        m.setMismatch(ser.tryUnmarshall(state, null, jsonset.get(key)).max(m).getMismatch());
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
    String java_class = getJavaClass(jso, clazz);

    AbstractSet abset = null;
    switch (java_class) {
      case "java.util.Set":
      case "java.util.AbstractSet":
      case "java.util.HashSet": {
        abset = new HashSet();
        break;
      }
      case "java.util.TreeSet": {
        abset = new TreeSet();
        break;
      }
      case "java.util.LinkedHashSet": {
        abset = new LinkedHashSet();
        break;
      }
      default:
        throw new UnmarshallException("not a Set");
    }
    JSONObject jsonset = getJsonObjectByMarshallingMode(jso, "set");

    Iterator i = jsonset.keys();
    String key = null;
    state.setSerialized(o, abset);
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        Object setElement = jsonset.get(key);
        abset.add(ser.unmarshall(state, null, setElement));
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("key " + i + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }
    return abset;
  }

}
