package cocaine.http;

/**
 * @author akirakozov
 */
public class HttpStatus {

    public static final int SC_100_CONTINUE = 100;
    public static final int SC_101_SWITCHING_PROTOCOLS = 101;
    public static final int SC_102_PROCESSING = 102;

    public static final int SC_200_OK = 200;
    public static final int SC_201_CREATED = 201;
    public static final int SC_202_ACCEPTED = 202;
    public static final int SC_203_NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int SC_204_NO_CONTENT = 204;
    public static final int SC_205_RESET_CONTENT = 205;
    public static final int SC_206_PARTIAL_CONTENT = 206;
    public static final int SC_207_MULTI_STATUS = 207;

    public static final int SC_300_MULTIPLE_CHOICES = 300;
    public static final int SC_301_MOVED_PERMANENTLY = 301;
    public static final int SC_302_MOVED_TEMPORARILY = 302;
    public static final int SC_303_SEE_OTHER = 303;
    public static final int SC_304_NOT_MODIFIED = 304;
    public static final int SC_305_USE_PROXY = 305;
    public static final int SC_307_TEMPORARY_REDIRECT = 307;

    public static final int SC_400_BAD_REQUEST = 400;
    public static final int SC_401_UNAUTHORIZED = 401;
    public static final int SC_402_PAYMENT_REQUIRED = 402;
    public static final int SC_403_FORBIDDEN = 403;
    public static final int SC_404_NOT_FOUND = 404;
    public static final int SC_405_METHOD_NOT_ALLOWED = 405;
    public static final int SC_406_NOT_ACCEPTABLE = 406;
    public static final int SC_407_PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int SC_408_REQUEST_TIMEOUT = 408;
    public static final int SC_409_CONFLICT = 409;
    public static final int SC_410_GONE = 410;
    public static final int SC_411_LENGTH_REQUIRED = 411;
    public static final int SC_412_PRECONDITION_FAILED = 412;
    public static final int SC_413_REQUEST_TOO_LONG = 413;
    public static final int SC_414_REQUEST_URI_TOO_LONG = 414;
    public static final int SC_415_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int SC_416_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int SC_417_EXPECTATION_FAILED = 417;

    public static final int SC_419_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    public static final int SC_420_METHOD_FAILURE = 420;
    public static final int SC_422_UNPROCESSABLE_ENTITY = 422;
    public static final int SC_423_LOCKED = 423;
    public static final int SC_424_FAILED_DEPENDENCY = 424;

    public static final int SC_500_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_501_NOT_IMPLEMENTED = 501;
    public static final int SC_502_BAD_GATEWAY = 502;
    public static final int SC_503_SERVICE_UNAVAILABLE = 503;
    public static final int SC_504_GATEWAY_TIMEOUT = 504;
    public static final int SC_505_HTTP_VERSION_NOT_SUPPORTED = 505;

    public static final int SC_507_INSUFFICIENT_STORAGE = 507;

    public static boolean is1xx(int status) {
        return 100 <= status && status <= 199;
    }

    public static boolean is2xx(int status) {
        return 200 <= status && status <= 299;
    }

    public static boolean is3xx(int status) {
        return 300 <= status && status <= 399;
    }

    public static boolean is4xx(int status) {
        return 400 <= status && status <= 499;
    }

    public static boolean is5xx(int status) {
        return 500 <= status && status <= 599;
    }
}
