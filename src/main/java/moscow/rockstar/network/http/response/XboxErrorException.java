package moscow.rockstar.network.http.response;

import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.network.http.HttpResponse;

/** Xbox Live error identified by the numeric X-Err/XErr code. */
public final class XboxErrorException extends ApiResponseException {
    public static final Map<Long, String> ERROR_NAMES_BY_CODE = new HashMap<>();
    public static final Map<Long, String> ERROR_MESSAGES_BY_CODE = new HashMap<>();
    private final long errorCode;

    public XboxErrorException(HttpResponse response, long errorCode) {
        super(response,
            ERROR_NAMES_BY_CODE.getOrDefault(errorCode, Long.toString(errorCode)),
            ERROR_MESSAGES_BY_CODE.getOrDefault(errorCode, "An unknown error occurred"));
        this.errorCode = errorCode;
    }

    public long getNumericErrorCode() {
        return errorCode;
    }

    static {
        ERROR_NAMES_BY_CODE.put(2279407619L, "AM_E_XASD_UNEXPECTED");
        ERROR_NAMES_BY_CODE.put(2279407620L, "AM_E_XASU_UNEXPECTED");
        ERROR_NAMES_BY_CODE.put(2279407621L, "AM_E_XAST_UNEXPECTED");
        ERROR_NAMES_BY_CODE.put(2279407622L, "AM_E_XSTS_UNEXPECTED");
        ERROR_NAMES_BY_CODE.put(2279407623L, "AM_E_XDEVICE_UNEXPECTED");
        ERROR_NAMES_BY_CODE.put(2279407624L, "AM_E_DEVMODE_NOT_AUTHORIZED");
        ERROR_NAMES_BY_CODE.put(2279407625L, "AM_E_NOT_AUTHORIZED");
        ERROR_NAMES_BY_CODE.put(2279407626L, "AM_E_FORBIDDEN");
        ERROR_NAMES_BY_CODE.put(2279407627L, "AM_E_UNKNOWN_TARGET");
        ERROR_NAMES_BY_CODE.put(2279407628L, "AM_E_NSAL_READ_FAILED");
        ERROR_NAMES_BY_CODE.put(2279407629L, "AM_E_TITLE_NOT_AUTHENTICATED");
        ERROR_NAMES_BY_CODE.put(2279407630L, "AM_E_TITLE_NOT_AUTHORIZED");
        ERROR_NAMES_BY_CODE.put(2279407631L, "AM_E_DEVICE_NOT_AUTHENTICATED");
        ERROR_NAMES_BY_CODE.put(2279407632L, "AM_E_INVALID_USER_INDEX");
        ERROR_NAMES_BY_CODE.put(2148916224L, "XO_E_DEVMODE_NOT_AUTHORIZED");
        ERROR_NAMES_BY_CODE.put(2148916225L, "XO_E_SYSTEM_UPDATE_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916226L, "XO_E_CONTENT_UPDATE_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916227L, "XO_E_ENFORCEMENT_BAN");
        ERROR_NAMES_BY_CODE.put(2148916228L, "XO_E_THIRD_PARTY_BAN");
        ERROR_NAMES_BY_CODE.put(2148916229L, "XO_E_ACCOUNT_PARENTALLY_RESTRICTED");
        ERROR_NAMES_BY_CODE.put(2148916230L, "XO_E_DEVICE_SUBSCRIPTION_NOT_ACTIVATED");
        ERROR_NAMES_BY_CODE.put(2148916232L, "XO_E_ACCOUNT_BILLING_MAINTENANCE_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916233L, "XO_E_ACCOUNT_CREATION_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916234L, "XO_E_ACCOUNT_TERMS_OF_USE_NOT_ACCEPTED");
        ERROR_NAMES_BY_CODE.put(2148916235L, "XO_E_ACCOUNT_COUNTRY_NOT_AUTHORIZED");
        ERROR_NAMES_BY_CODE.put(2148916236L, "XO_E_ACCOUNT_AGE_VERIFICATION_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916237L, "XO_E_ACCOUNT_CURFEW");
        ERROR_NAMES_BY_CODE.put(2148916238L, "XO_E_ACCOUNT_ZEST_MAINTENANCE_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916239L, "XO_E_ACCOUNT_CSV_TRANSITION_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916240L, "XO_E_ACCOUNT_MAINTENANCE_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916241L, "XO_E_ACCOUNT_TYPE_NOT_ALLOWED");
        ERROR_NAMES_BY_CODE.put(2148916242L, "XO_E_CONTENT_ISOLATION");
        ERROR_NAMES_BY_CODE.put(2148916243L, "XO_E_ACCOUNT_NAME_CHANGE_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916244L, "XO_E_DEVICE_CHALLENGE_REQUIRED");
        ERROR_NAMES_BY_CODE.put(2148916256L, "XO_E_EXPIRED_DEVICE_TOKEN");
        ERROR_NAMES_BY_CODE.put(2148916257L, "XO_E_EXPIRED_TITLE_TOKEN");
        ERROR_NAMES_BY_CODE.put(2148916258L, "XO_E_EXPIRED_USER_TOKEN");
        ERROR_NAMES_BY_CODE.put(2148916259L, "XO_E_INVALID_DEVICE_TOKEN");
        ERROR_NAMES_BY_CODE.put(2148916260L, "XO_E_INVALID_TITLE_TOKEN");
        ERROR_NAMES_BY_CODE.put(2148916261L, "XO_E_INVALID_USER_TOKEN");
        ERROR_MESSAGES_BY_CODE.put(2148916227L, "Your account was banned by Xbox for violating one or more Community Standards for Xbox.");
        ERROR_MESSAGES_BY_CODE.put(2148916229L, "Your account is currently restricted and your guardian has not given you permission to play online. Login to https://account.microsoft.com/family/ and have your guardian change your permissions.");
        ERROR_MESSAGES_BY_CODE.put(2148916233L, "Your account doesn't have an Xbox profile. Please create one at https://www.xbox.com/live");
        ERROR_MESSAGES_BY_CODE.put(2148916234L, "Your account has not accepted Xbox's Terms of Service. Please login at https://www.xbox.com/live and accept them.");
        ERROR_MESSAGES_BY_CODE.put(2148916235L, "Your account is from a country where Xbox Live is not available/banned.");
        ERROR_MESSAGES_BY_CODE.put(2148916236L, "Your account requires proof of age. Please login to https://login.live.com/login.srf and provide proof of age.");
        ERROR_MESSAGES_BY_CODE.put(2148916237L, "Your account has reached the its limit for playtime. Your account has been blocked from logging in.");
        ERROR_MESSAGES_BY_CODE.put(2148916238L, "Your account is a child (under 18) and cannot proceed unless the account is added to a Family by an adult.");
    }
}
