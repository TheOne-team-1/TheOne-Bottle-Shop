package one.theone.server.common.util;

public class Constants {

    private Constants() {
    }

    //region Freebie 관련 Lock
    public static final String FREEBIE_LOCK_KEY = "freebie:lock:";
    public static final Long FREEBIE_LOCK_WAIT_TIME = 3L;
    public static final Long FREEBIE_LOCK_LEASE_TIME = 3L;
    public static final Long FREEBIE_LOCK_WATCH_DOG_LEASE_TIME = 1L;
    //endregion

    //region Coupon 관련 Lock
    public static final String COUPON_LOCK_KEY = "coupon:lock:";
    public static final Long COUPON_LOCK_WAIT_TIME = 5L;
    public static final Long COUPON_LOCK_LEASE_TIME = 5L;
    public static final Long COUPON_LOCK_WATCH_DOG_LEASE_TIME = 1L;
    //endregion
}


