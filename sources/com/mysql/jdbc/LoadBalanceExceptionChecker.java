package com.mysql.jdbc;

import java.sql.SQLException;

public interface LoadBalanceExceptionChecker extends Extension {
    boolean shouldExceptionTriggerFailover(SQLException sQLException);
}
