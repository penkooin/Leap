package org.chaostocosmos.http;

public enum RES_CODE {
    RES200(200),
    RES201(201),
    RES202(202),
    RES203(203),
    RES205(205),
    RES206(206),
    RES300(300),
    RES301(301),
    RES307(307),
    RES308(308),
    RES400(200),
    RES401(401),
    RES402(402),
    RES403(403),
    RES404(404),
    RES405(405),
    RES406(406),
    RES407(407),
    RES408(408),
    RES409(409),
    RES412(412),
    RES413(413),
    RES414(414),
    RES415(415),
    RES417(417),
    RES418(418),
    RES421(421),
    RES423(423),
    RES429(429),
    RES431(431),
    RES451(451),
    RES500(500),
    RES501(501),
    RES502(502),
    RES503(503),
    RES504(504),
    RES505(505),
    RES507(507),
    RES508(508),
    RES510(510),
    RES511(511);

    int code;
    String resMesg;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return this.resMesg;
    }

    RES_CODE(int code) {
        this.code = code;
        this.resMesg = Context.getInstance().getHttpMsg(code);
    }    
}
