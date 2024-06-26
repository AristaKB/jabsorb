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

import java.sql.Timestamp;
import java.util.Date;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises date and time values
 */
public class DateSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { Date.class,
      Timestamp.class, java.sql.Date.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

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
    long time;
    if (o instanceof Date)
    {
      time = ((Date) o).getTime();
    }
    else
    {
      throw new MarshallException("cannot marshall date using class "
          + o.getClass());
    }
    JSONObject obj = new JSONObject();
    try
    {
      if (ser.getMarshallClassHints())
      {
        obj.put("javaClass", o.getClass().getName());
      }
      obj.put("time", time);
    }
    catch (JSONException e)
    {
      throw (MarshallException) new MarshallException(e.getMessage()).initCause(e);
    }
    return obj;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class;
    try
    {
      java_class = jso.getString("javaClass");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("no type hint", e);
    }
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    if (!(java_class.equals("java.util.Date")))
    {
      throw new UnmarshallException("not a Date");
    }
    state.setSerialized(o, ObjectMatch.OKAY);
    return ObjectMatch.OKAY;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    long time;
    try
    {
      time = jso.getLong("time");
    }
    catch(JSONException e)
    {
      throw new UnmarshallException("Could not get the time in date serialiser", e);
    }
    if (jso.has("javaClass"))
    {
      try
      {
        clazz = Class.forName(jso.getString("javaClass"));
      }
      catch (ClassNotFoundException e)
      {
        throw new UnmarshallException(e.getMessage(), e);
      }
      catch(JSONException e)
      {
        throw new UnmarshallException("Could not find javaClass", e);
      }
    }
    Object returnValue = null;
    if (Date.class.equals(clazz))
    {
      returnValue = new Date(time);
    }
    else if (Timestamp.class.equals(clazz))
    {
      returnValue = new Timestamp(time);
    }
    else if (java.sql.Date.class.equals(clazz))
    {
      returnValue = new java.sql.Date(time);
    }
    if (returnValue == null)
    {
      throw new UnmarshallException("invalid class " + clazz);
    }
    state.setSerialized(o, returnValue);
    return returnValue;
   }

}
