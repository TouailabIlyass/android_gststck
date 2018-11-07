package com.mysql.jdbc;

import java.sql.SQLException;

public class NdbLoadBalanceExceptionChecker extends StandardLoadBalanceExceptionChecker {
    public boolean shouldExceptionTriggerFailover(SQLException ex) {
        if (!super.shouldExceptionTriggerFailover(ex)) {
            if (!checkNdbException(ex)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkNdbException(SQLException ex) {
        if (!ex.getMessage().startsWith("Lock wait timeout exceeded")) {
            if (!ex.getMessage().startsWith("Got temporary error") || !ex.getMessage().endsWith("from NDB")) {
                return false;
            }
        }
        return true;
    }
}
