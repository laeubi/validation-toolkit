/*
 * #%L
 * Ant Validation Toolkit
 * %%
 * Copyright (C) 2013 Christoph Läubrich
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * 
 */
package de.laeubisoft.tools.ant.validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * @author Christoph Läubrich
 */
public class Tools {

    /**
     * Extract the "real" Object from JaxB
     * 
     * @param object
     * @return
     */
    static Object getObject(Object object) {
        if (object instanceof JAXBElement<?>) {
            object = ((JAXBElement<?>) object).getValue();
        }
        return object;
    }

    /**
     * Trimms a String if not <code>null</code>, returns empty string otherwhise
     * 
     * @param str
     * @return
     */
    public static String trim(String str) {
        if (str != null) {
            return str.trim();
        }
        return "";
    }

    /**
     * Creates a {@link RequestEntity} that can be used for submitting a file
     * 
     * @param params
     *            the params to use
     * @param methodParams
     *            the {@link HttpMethodParams} of the requesting method
     * @return {@link RequestEntity} that can be used for submitting the given
     *         file via Multipart
     * @throws IOException
     *             if something is wrong with the file...
     */
    public static RequestEntity createFileUpload(File file, String filePartName, String charset, List<NameValuePair> params, HttpMethodParams methodParams)
            throws IOException {
        if (file == null) {
            throw new FileNotFoundException("file not present!");
        }
        List<Part> parts = nvToParts(params);
        FilePart fp = new FilePart(filePartName, file);
        fp.setContentType(URLConnection.guessContentTypeFromName(file.getName()));
        if (charset != null) {
            fp.setCharSet(charset);
        }
        parts.add(fp);
        return new MultipartRequestEntity(parts.toArray(new Part[0]), methodParams);
    }

    public static List<Part> nvToParts(List<NameValuePair> params) {
        List<Part> parts = new ArrayList<Part>();
        for (NameValuePair nameValuePair : params) {
            parts.add(new StringPart(nameValuePair.getName(), nameValuePair.getValue()));
        }
        return parts;
    }

}
