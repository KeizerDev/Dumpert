/*
 * Copyright 2014 Jari Zwarts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jari.dumpert.thirdparty;

import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Take an object and serialize and then save it to preferences
 * @author John Matthews
 *
 */
public class SerializeObject {
//    private final static String TAG = "SerializeObject";

    /**
     * Create a String from the Object using Base64 encoding
     * @param object - any Object that is Serializable
     * @return - Base64 encoded string.
     */
    public static String objectToString(Serializable object) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(out).writeObject(object);
            byte[] data = out.toByteArray();
            out.close();

            out = new ByteArrayOutputStream();
            Base64OutputStream b64 = new Base64OutputStream(out,0);
            b64.write(data);
            b64.close();
            out.close();

            return new String(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a generic object that needs to be cast to its proper object
     * from a Base64 ecoded string.
     *
     * @param encodedObject String
     * @return Object
     */
    public static Object stringToObject(String encodedObject) {
        try {
            return new ObjectInputStream(new Base64InputStream(
                    new ByteArrayInputStream(encodedObject.getBytes()), 0)).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
