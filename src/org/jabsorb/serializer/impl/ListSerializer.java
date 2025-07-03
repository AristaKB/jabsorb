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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.MarshallingModeContext;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises lists
 * 
 * TODO: if this serialises a superclass does it need to also specify the
 * subclasses?
 */
public class ListSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { List.class,
      ArrayList.class, LinkedList.class, Vector.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class, JSONArray.class };

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) ||
            ((jsonClazz == null ||
                    ( jsonClazz == JSONObject.class || jsonClazz == JSONArray.class))
                    && List.class.isAssignableFrom(clazz)));
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
    // marshall as per the mode
    switch (MarshallingModeContext.get()) {
      case JABSORB:
        return marshallJabsorb(state, o);
      case STANDARD_REST:
        return marshallREST(state, o);
      default:
        throw new MarshallException("Invalid marshall mode: " + MarshallingModeContext.get());
    }
  }

  /**
   * Marshall as per jabsorb convention
   * @param state
   * @param o
   * @return
   * @throws MarshallException
   */
  public Object marshallJabsorb(SerializerState state, Object o)
          throws MarshallException
  {
    JSONObject obj = new JSONObject();
    JSONArray arr = new JSONArray();
    setJavaClassToJSONObject(o, obj);
    try
    {
      obj.put("list", arr);
    }
    catch (JSONException e)
    {
      throw new MarshallException("Error setting list: " + e);
    }
    int index = 0;
    marshallListToJSONArray(state, o, arr, index, "list");
    return obj;
  }

  /**
   * Marshall as per standard REST convention
   * @param state
   * @param o
   * @return
   * @throws MarshallException
   */
  public Object marshallREST(SerializerState state, Object o)
          throws MarshallException
  {
    JSONArray arr = new JSONArray();
    int index = 0;
    marshallListToJSONArray(state, o, arr, index, null);
    return arr;
  }

  /**
   * Marshall input list into JSONArray
   * pushes objects into state under input parentNode, pops state only if parentNode is not null.
   * @param state
   * @param o
   * @param arr
   * @param index
   * @param parentNode
   * @throws MarshallException
   */
  private void marshallListToJSONArray(SerializerState state, Object o, JSONArray arr, int index, String parentNode) throws MarshallException {
    List list = (List) o;
    state.push(o, arr, parentNode);
    try {
      for (Object object : list) {
        Object json = ser.marshall(state, arr, object, index);
        if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json) {
          arr.put(json);
        } else {
          // put a slot where the object would go, so it can be fixed up properly in the fix up phase
          arr.put(JSONObject.NULL);
        }
        index++;
      }
    }
    catch (MarshallException e)
    {
      throw new MarshallException("element " + index, e);
    }
    finally {
      // pop can only be done if state.push() was done against a parent ref
      if (parentNode != null) {
        state.pop();
      }
    }
  }

  // TODO: try unMarshall and unMarshall share 90% code. Put in into an
  // intermediate function.
  // TODO: Also cache the result somehow so that an unmarshall
  // following a tryUnmarshall doesn't do the same work twice!
  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONArray jsonlist = null;

    // unmarshall as per the mode
    switch (MarshallingModeContext.get()) {
      case JABSORB:
        sanityCheckForListClass((JSONObject) o);
        jsonlist = getJsonArrayFromListNode(o);
        break;
      case STANDARD_REST:
        jsonlist = (JSONArray) o;
        break;
      default:
        throw new UnmarshallException("Invalid unmarshall mode: " + MarshallingModeContext.get());
    }

    int i = 0;
    ObjectMatch m = new ObjectMatch(-1);
    state.setSerialized(o, m);
    try
    {
      for (; i < jsonlist.length(); i++)
      {
        m.setMismatch(ser.tryUnmarshall(state, null, jsonlist.get(i)).max(m).getMismatch());
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
    }
    return m;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONArray jsonlist = null;
    AbstractList al = null;

    // unmarshall as per the mode
    switch (MarshallingModeContext.get()) {
      case JABSORB:
        sanityCheckForListClass((JSONObject) o);
        al = getAbstractListByClass(getJavaClass((JSONObject) o));
        jsonlist = getJsonArrayFromListNode(o);
        break;
      case STANDARD_REST:
        // rely on input clazz to get the list class type
        if (clazz != null)
          al = getAbstractListByClass(clazz.getName());
        jsonlist = (JSONArray) o;
        break;
      default:
        throw new UnmarshallException("Invalid unarshall mode: " + MarshallingModeContext.get());
    }

    if (al == null) {
      throw new UnmarshallException("not a List");
    }

    state.setSerialized(o, al);

    int i = 0;
    try
    {
      for (; i < jsonlist.length(); i++)
      {
        al.add(ser.unmarshall(state, null, jsonlist.get(i)));
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
    }
    return al;
  }

  private static AbstractList getAbstractListByClass(String javaClass) throws UnmarshallException {
    AbstractList al;
    if (javaClass.equals("java.util.List")
            || javaClass.equals("java.util.AbstractList")
            || javaClass.equals("java.util.ArrayList")) {
      al = new ArrayList();
    } else if (javaClass.equals("java.util.LinkedList")) {
      al = new LinkedList();
    } else if (javaClass.equals("java.util.Vector")) {
      al = new Vector();
    } else {
      throw new UnmarshallException("not a List");
    }
    return al;
  }

  private static JSONArray getJsonArrayFromListNode(Object o) throws UnmarshallException {
    JSONObject jso = (JSONObject) o;

    JSONArray jsonlist;
    try
    {
      jsonlist = jso.getJSONArray("list");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read list: " + e.getMessage(), e);
    }
    if (jsonlist == null)
    {
      throw new UnmarshallException("list missing");
    }
    return jsonlist;
  }

  private static String getJavaClass(JSONObject jso) throws UnmarshallException {
    String javaClass;
    try
    {
      javaClass = jso.getString("javaClass");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read javaClass", e);
    }
    if (javaClass == null)
    {
      throw new UnmarshallException("no type hint");
    }
    return javaClass;
  }

  private static void sanityCheckForListClass(JSONObject obj) throws UnmarshallException {
    String javaClass = getJavaClass(obj);
    if (!(javaClass.equals("java.util.List")
            || javaClass.equals("java.util.AbstractList")
            || javaClass.equals("java.util.LinkedList")
            || javaClass.equals("java.util.ArrayList") || javaClass
            .equals("java.util.Vector")))
    {
      throw new UnmarshallException("not a List");
    }
  }

}
