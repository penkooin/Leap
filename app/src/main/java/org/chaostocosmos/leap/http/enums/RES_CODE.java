package org.chaostocosmos.leap.http.enums;

import org.chaostocosmos.leap.http.context.Context;

/**
 * HTTP response code enum
 */
public enum RES_CODE {
    // http200: OK.   {}
    RES200(200),
    // http201: Created.   {}
    RES201(201),
    // http202: Accepted.   {}
    RES202(202),
    // http203: No Content.   {}
    RES203(203),
    // http205: Reset Content.   {}
    RES205(205),
    // http206: Partial Content. (RFC 7233)   {}
    RES206(206),
    // http300: Multiple Choices.   {}
    RES300(300),
    // http301: Moved Permanently.   {}
    RES301(301),
    // http307: Temporary Redirect. (since http/1.1)   {}
    RES307(307),
    // http308: Permanent Redirect. (RFC 7538)   {}
    RES308(308),
    // http400: Bad Request.   {}
    RES400(400),
    // http401: Unauthorized. (RFC 7235)   {}   
    RES401(401),
    // http402: Payment Required.   {}
    RES402(402),
    // http403: Forbidden.   {}
    RES403(403),
    // http404: Resource Not Found.   {}
    RES404(404),
    // http405: Method Not Allowed.   {}
    RES405(405),
    // http406: Not Acceptable.   {}
    RES406(406),
    // http407: Proxy Authentication Required. (RFC 7235)   {}
    RES407(407),
    // http408: Request Timeout.   {}
    RES408(408),
    // http409: Conflict.   {}
    RES409(409),
    // http412: Precondition Failed. (RFC 7232)   {}
    RES412(412),
    // http413: Payload Too Large. (RFC 7231)   {}
    RES413(413),
    // http414: URI Too Long. (RFC 7231)   {}
    RES414(414),
    // http415: Unsupported Media Type. (RFC 7231)   {}
    RES415(415),
    // http417: Expectation Failed.   {}
    RES417(417),
    // http418: I'm a teapot. (RFC 2324, RFC 7168)   {}
    RES418(418),
    // http421: Misdirected Request. (RFC 7540)   {}
    RES421(421),
    // http423: Locked. (WebDAV; RFC 4918)   {}
    RES423(423),
    // http429: Too Many Requests. (RFC 6585)   {}
    RES429(429),
    // http431: Request Header Fields Too Large. (RFC 6585)   {}
    RES431(431),
    // http451: Unavailable For Legal Reasons. (RFC 7725)   {}
    RES451(451),
    // http500: Internal Server Error.   {}
    RES500(500),
    // http501: Not Implemented.   {}
    RES501(501),
    // http502: Bad Gateway.   {}
    RES502(502),
    // http503: Service Unavailable.   {}
    RES503(503),
    // http504: Gateway Timeout.   {}
    RES504(504),
    // http505: http Version Not Supported.   {}
    RES505(505),
    // http507: Insufficient Storage. (WebDAV; RFC 4918)   {}
    RES507(507),
    // http508: Loop Detected. (WebDAV; RFC 5842)   {}
    RES508(508),
    // http510: Not Extended. (RFC 2774)   {}
    RES510(510),
    // http511: Network Authentication Required. (RFC 6585)   {}     
    RES511(511);
    /**
     * Response code
     */
    int code;

    /**
     * Response message
     */
    String msg;

    /**
     * Get response code
     * @return
     */
    public int code() {
        return code;
    }

    /**
     * Get response message
     * @return
     */
    public String msg() {
        return this.msg;
    }
    
    /**
     * Initializer
     * @param code
     */
    RES_CODE(int code) {
        this.code = code;
        this.msg = Context.getMessages().getHttpMsg(code).toString().trim();
    }    
}
