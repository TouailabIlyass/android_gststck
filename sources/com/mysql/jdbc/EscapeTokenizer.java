package com.mysql.jdbc;

public class EscapeTokenizer {
    private static final char CHR_BEGIN_TOKEN = '{';
    private static final char CHR_COMMENT = '-';
    private static final char CHR_CR = '\r';
    private static final char CHR_DBL_QUOTE = '\"';
    private static final char CHR_END_TOKEN = '}';
    private static final char CHR_ESCAPE = '\\';
    private static final char CHR_LF = '\n';
    private static final char CHR_SGL_QUOTE = '\'';
    private static final char CHR_VARIABLE = '@';
    private int bracesLevel = 0;
    private boolean emittingEscapeCode = false;
    private boolean inQuotes = false;
    private int pos = 0;
    private char quoteChar = '\u0000';
    private boolean sawVariableUse = false;
    private String source = null;
    private int sourceLength = 0;

    public EscapeTokenizer(String source) {
        this.source = source;
        this.sourceLength = source.length();
        this.pos = 0;
    }

    public synchronized boolean hasMoreTokens() {
        return this.pos < this.sourceLength;
    }

    public synchronized String nextToken() {
        StringBuilder tokenBuf = new StringBuilder();
        boolean backslashEscape = false;
        if (this.emittingEscapeCode) {
            tokenBuf.append("{");
            this.emittingEscapeCode = false;
        }
        while (this.pos < this.sourceLength) {
            char c = this.source.charAt(this.pos);
            if (c == CHR_ESCAPE) {
                tokenBuf.append(c);
                backslashEscape = !backslashEscape;
            } else if ((c == CHR_SGL_QUOTE || c == CHR_DBL_QUOTE) && !backslashEscape) {
                tokenBuf.append(c);
                if (!this.inQuotes) {
                    this.inQuotes = true;
                    this.quoteChar = c;
                } else if (c == this.quoteChar) {
                    if (this.pos + 1 >= this.sourceLength || this.source.charAt(this.pos + 1) != this.quoteChar) {
                        this.inQuotes = false;
                    } else {
                        tokenBuf.append(c);
                        this.pos++;
                    }
                }
            } else {
                if (c != CHR_LF) {
                    if (c != CHR_CR) {
                        if (!(this.inQuotes || backslashEscape)) {
                            if (c == CHR_COMMENT) {
                                tokenBuf.append(c);
                                if (this.pos + 1 < this.sourceLength && this.source.charAt(this.pos + 1) == CHR_COMMENT) {
                                    while (true) {
                                        int i = this.pos + 1;
                                        this.pos = i;
                                        if (i >= this.sourceLength || c == CHR_LF || c == CHR_CR) {
                                            this.pos--;
                                        } else {
                                            c = this.source.charAt(this.pos);
                                            tokenBuf.append(c);
                                        }
                                    }
                                    this.pos--;
                                }
                            } else if (c == CHR_BEGIN_TOKEN) {
                                this.bracesLevel++;
                                if (this.bracesLevel == 1) {
                                    this.emittingEscapeCode = true;
                                    this.pos++;
                                    return tokenBuf.toString();
                                }
                                tokenBuf.append(c);
                            } else if (c == CHR_END_TOKEN) {
                                tokenBuf.append(c);
                                this.bracesLevel--;
                                if (this.bracesLevel == 0) {
                                    this.pos++;
                                    return tokenBuf.toString();
                                }
                            } else if (c == CHR_VARIABLE) {
                                this.sawVariableUse = true;
                            }
                        }
                        tokenBuf.append(c);
                        backslashEscape = false;
                    }
                }
                tokenBuf.append(c);
                backslashEscape = false;
            }
            this.pos++;
        }
        return tokenBuf.toString();
    }

    boolean sawVariableUse() {
        return this.sawVariableUse;
    }
}
