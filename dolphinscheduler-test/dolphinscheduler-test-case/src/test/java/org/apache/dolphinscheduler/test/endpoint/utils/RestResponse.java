package org.apache.dolphinscheduler.test.endpoint.utils;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.dolphinscheduler.test.endpoint.base.IRestResponse;
import org.apache.dolphinscheduler.test.endpoint.api.common.FormParam;

import static org.hamcrest.Matchers.equalTo;

public class RestResponse<T> implements IRestResponse<T> {
    private T data;
    private Response response;
    private Exception e;

    public RestResponse(Class<T> t, Response response) {
        this.response = response;
        try {
            this.data = t.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("There should be a default constructor in the Response POJO");
        }
    }

    public String getContent() {
        return response.getBody().asString();
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public boolean isSuccessful() {
        int code = response.getStatusCode();
        return code == 200 || code == 201 || code == 202 || code == 203 || code == 204 || code == 205;
    }

    public ValidatableResponse isResponseSuccessful() {
        return response.then().
                body(FormParam.CODE.getParam(), equalTo(0));
    }

    public String getStatusDescription() {
        return response.getStatusLine();
    }

    public Response getResponse() {
        return response;
    }


    public T getBody() {
        try {
            data = (T) response.getBody().as(data.getClass());
        } catch (Exception e) {
            this.e = e;
        }
        return data;
    }

    public Exception getException() {
        return e;
    }

    @Override
    public String toString() {
        return response.asString();
    }
}