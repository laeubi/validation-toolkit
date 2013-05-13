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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3.css.validator.CSSValidationResponse;
import org.w3.css.validator.Error;
import org.w3.css.validator.ErrorList;
import org.w3.css.validator.ObjectFactory;
import org.w3.css.validator.Result;
import org.w3.css.validator.ValidationErrors;
import org.w3.css.validator.ValidationWarnings;
import org.w3.css.validator.Warning;
import org.w3.css.validator.WarningList;
import org.w3.soap.envelope.Body;
import org.w3.soap.envelope.Envelope;

/**
 * Provides a task for automatic checking of CSS files
 * 
 * @author Christoph Läubrich
 */
public class W3CCSSValidationTask extends Task {

    /**
     * The URL of the public online validator
     */
    private static final String W3_ORG_VALIDATOR        = "http://jigsaw.w3.org/css-validator/";

    /**
     * We want soap !
     */
    private static final String VALIDATOR_FORMAT_OUTPUT = "soap12";

    /**
     * The URL of the document to validate,either this parameter, or
     * uploaded_file, or fragment must be given.
     */
    private URL                 uri;

    private String              errorFormat             = "[ERROR] [{0}][{1}] Line {2}: {3}, context = {4}, type = {5}, subtype = {6}, skipped = {7}";
    private String              warningFormat           = "[WARNING] [{0}][{1}] Line {2}: {3}";

    /**
     * URL of the validator server to use
     */
    private String              validator               = W3_ORG_VALIDATOR;

    /**
     * Das zu prüfende Dokument; hier ist nur CSS erlaubt. Keine. Entweder
     * dieser Parameter oder der uri-Parameter müssen aber vorhanden sein.
     */
    private String              text;
    /**
     * Das medium für die Validierung, z.B. screen, print, braille... all
     */
    private String              usermedium;

    /**
     * Das CSS-Profil für die Validierung. Das kann css1, css2, css21, css3,
     * svg, svgbasic, svgtiny, mobile, atsc-tv, tv oder none sein. die letzte
     * W3C Recommendation: CSS 2
     */
    private String              profile;

    /**
     * Die Ausgabesprache; zur Zeit werden unterstützt: en, fr, it, ko, ja, es,
     * zh-cn, nl, de. Englisch (en).
     */
    private String              lang;
    /**
     * Menge der ausgegebenen Warnungen. no für keine Warnungen, 0 für wenige,
     * 1oder 2 für mehr Warnungen
     */
    private String              warning;

    private File                file;

    private boolean             fail;

    /**
     * @param warningFormat
     *            the new value for warningFormat
     */
    public void setWarningFormat(String warningFormat) {
        this.warningFormat = warningFormat;
    }

    /**
     * @param fail
     *            the new value for fail
     */
    public void setFail(boolean fail) {
        this.fail = fail;
    }

    /**
     * @param errorFormat
     *            the new value for errorFormat
     */
    public void setErrorFormat(String errorFormat) {
        this.errorFormat = errorFormat;
    }

    /**
     * @param file
     *            the new value for file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @param validator
     *            the new value for validator
     */
    public void setValidator(String validator) {
        this.validator = validator;
    }

    /**
     * @param warning
     *            the new value for warning
     */
    public void setWarningLevel(String warning) {
        this.warning = warning;
    }

    /**
     * @param lang
     *            the new value for lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * @param profile
     *            the new value for profile
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * @param usermedium
     *            the new value for usermedium
     */
    public void setUsermedium(String usermedium) {
        this.usermedium = usermedium;
    }

    /**
     * @param text
     *            the new value for text
     */
    public void setCSSText(String text) {
        this.text = text;
    }

    /**
     * @param uri
     *            the new value for uri
     */
    public void setUri(URL uri) {
        this.uri = uri;
    }

    @Override
    public void execute() throws BuildException {
        validateParameter();
        try {
            InputStream connection = buildConnection(uri);
            Unmarshaller unmarshaller = JAXBContext.newInstance(Envelope.class, ObjectFactory.class).createUnmarshaller();
            Object object = Tools.getObject(unmarshaller.unmarshal(connection));
            if (W3_ORG_VALIDATOR.equals(validator)) {
                //The W3C recommends to at least wait one second between automatic requests to their public service...
                //So we sleep here for one second to comply with this
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    //We don't care then...
                    return;
                }
            }
            if (object instanceof Envelope) {
                Body body = ((Envelope) object).getBody();
                for (Object elem : body.getAny()) {
                    Object any = Tools.getObject(elem);
                    if (any instanceof CSSValidationResponse) {
                        handleResponse((CSSValidationResponse) any);
                    }
                }
            }
        } catch (JAXBException e) {
            throw new BuildException("problem handling XML");
        } catch (IOException e) {
            throw new BuildException("problem communcating with server", e);
        }
    }

    /**
     * Validates the parameter and throws exception if something is invalid
     * 
     * @throws BuildException
     */
    private void validateParameter() throws BuildException {
        int notNullSource = 0;
        if (uri != null) {
            notNullSource++;
        }
        if (text != null) {
            notNullSource++;
        }
        if (file != null) {
            notNullSource++;
        }
        if (notNullSource == 0) {
            throw new BuildException("at least one of 'uri', 'cssText' or 'file' must be given!");
        }
        if (notNullSource > 1) {
            throw new BuildException("Only one of 'uri', 'cssText' or 'file' can be given!");
        }
    }

    /**
     * Creates the actual request to the validation server for a given
     * {@link URL} and returns an inputstream the result can be read from
     * 
     * @param uriToCheck
     *            the URL to check (or <code>null</code> if text or file should
     *            be used as input
     * @return the stream to read the response from
     * @throws IOException
     *             if unrecoverable communication error occurs
     * @throws BuildException
     *             if server returned unexspected results
     */
    private InputStream buildConnection(final URL uriToCheck) throws IOException, BuildException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("output", VALIDATOR_FORMAT_OUTPUT));
        if (uriToCheck != null) {
            params.add(new NameValuePair("uri", uriToCheck.toString()));
        } else {
            if (text != null) {
                params.add(new NameValuePair("text", text));
            }
        }
        if (usermedium != null) {
            params.add(new NameValuePair("usermedium", usermedium));
        }
        if (profile != null) {
            params.add(new NameValuePair("profile", profile));
        }
        if (lang != null) {
            params.add(new NameValuePair("lang", lang));
        }
        if (warning != null) {
            params.add(new NameValuePair("warning", warning));
        }
        HttpClient httpClient = new HttpClient();
        HttpMethodBase method;
        if (uriToCheck != null) {
            //URIs must be checked via traditonal GET...
            GetMethod getMethod = new GetMethod(validator);
            getMethod.setQueryString(params.toArray(new NameValuePair[0]));
            method = getMethod;
        } else {
            PostMethod postMethod = new PostMethod(validator);
            if (text != null) {
                //Text request must be multipart encoded too...
                postMethod.setRequestEntity(new MultipartRequestEntity(Tools.nvToParts(params).toArray(new Part[0]), postMethod.getParams()));
            } else {
                //Finally files must be checked with multipart-forms....
                postMethod.setRequestEntity(Tools.createFileUpload(file, "file", null, params, postMethod.getParams()));
            }
            method = postMethod;
        }
        int result = httpClient.executeMethod(method);
        if (result == HttpStatus.SC_OK) {
            return method.getResponseBodyAsStream();
        } else {
            throw new BuildException("Server returned " + result + " " + method.getStatusText());
        }

    }

    /**
     * @param any
     */
    private void handleResponse(CSSValidationResponse response) {
        log("Checkedby:      " + response.getCheckedby());
        log("Csslevel:       " + response.getCsslevel());
        log("EncodingStyle:  " + response.getEncodingStyle());
        log("Date:           " + response.getDate());
        log("URI:            " + response.getUri());
        log("Validity:       " + response.isValidity());
        Result result = response.getResult();
        ValidationErrors validationErrors = result.getErrors();
        if (validationErrors != null) {
            for (ErrorList errorList : validationErrors.getErrorlist()) {
                for (Error error : errorList.getError()) {
                    log(MessageFormat.format(errorFormat, errorList.getUri(), error.getLevel(), error.getLine(), Tools.trim(error.getMessage()), Tools.trim(error.getContext()), Tools.trim(error.getErrortype()), Tools.trim(error.getErrorsubtype()), Tools.trim(error.getSkippedstring())), Project.MSG_ERR);
                }
            }
        }
        ValidationWarnings validationWarnings = result.getWarnings();
        if (validationWarnings != null) {
            for (WarningList warningList : validationWarnings.getWarninglist()) {
                for (Warning warning : warningList.getWarning()) {

                    log(MessageFormat.format(warningFormat, warningList.getUri(), warning.getLevel(), warning.getLine(), warning.getMessage()), Project.MSG_WARN);
                }
            }
        }
        if (fail && !response.isValidity()) {
            throw new BuildException("Validation produced errors");
        }
    }

}
