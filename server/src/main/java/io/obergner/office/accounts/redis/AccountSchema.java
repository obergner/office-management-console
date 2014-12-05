package io.obergner.office.accounts.redis;

import java.util.UUID;

public class AccountSchema {

    public static class Keys {

        public static final String ACCOUNT_MMA_INDEX = "account:mma:index";

        public static String accountUuid(final String uuid) {
            return "account:uuid:" + uuid;
        }

        public static String accountUuid(final UUID uuid) {
            return accountUuid(uuid.toString());
        }
    }

    public static class Fields {

        public static final String UUID = "uuid";

        public static final String NAME = "name";

        public static final String MMA_ID = "mma";

        public static final String CREATED_AT = "created_at";

        public static final String ALLOWED_OUT_CHANNELS = "outchannels";
    }
}
