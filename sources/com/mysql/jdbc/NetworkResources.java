package com.mysql.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class NetworkResources {
    private final Socket mysqlConnection;
    private final InputStream mysqlInput;
    private final OutputStream mysqlOutput;

    protected NetworkResources(Socket mysqlConnection, InputStream mysqlInput, OutputStream mysqlOutput) {
        this.mysqlConnection = mysqlConnection;
        this.mysqlInput = mysqlInput;
        this.mysqlOutput = mysqlOutput;
    }

    protected final void forceClose() {
        NetworkResources this;
        NetworkResources this2;
        try {
            if (this.mysqlInput != null) {
                this.mysqlInput.close();
            }
            try {
                if (!(this.mysqlConnection == null || this.mysqlConnection.isClosed() || this.mysqlConnection.isInputShutdown())) {
                    try {
                        this.mysqlConnection.shutdownInput();
                    } catch (UnsupportedOperationException e) {
                    }
                }
                this = this;
            } catch (IOException e2) {
                this = this;
            }
        } catch (IOException e3) {
        } catch (Throwable th) {
            if (!(this.mysqlConnection == null || this.mysqlConnection.isClosed() || this.mysqlConnection.isInputShutdown())) {
                try {
                    this.mysqlConnection.shutdownInput();
                } catch (UnsupportedOperationException e4) {
                }
            }
        }
        try {
            if (this.mysqlOutput != null) {
                this.mysqlOutput.close();
            }
            try {
                if (!(this.mysqlConnection == null || this2.mysqlConnection.isClosed() || this2.mysqlConnection.isOutputShutdown())) {
                    try {
                        this2.mysqlConnection.shutdownOutput();
                    } catch (UnsupportedOperationException e5) {
                    }
                }
            } catch (IOException e6) {
            }
        } catch (IOException e7) {
            this2 = this;
        } catch (Throwable th2) {
            if (!(this.mysqlConnection == null || this.mysqlConnection.isClosed() || this.mysqlConnection.isOutputShutdown())) {
                try {
                    this.mysqlConnection.shutdownOutput();
                } catch (UnsupportedOperationException e8) {
                }
            }
        }
        try {
            if (this2.mysqlConnection != null) {
                this2.mysqlConnection.close();
            }
        } catch (IOException e9) {
        }
    }
}
