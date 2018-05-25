package cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions;

/**
 * Internal use only class.
 * Created by wizzer on 2018/5/13.
 */
public class Token {

    static final Token EMPTY = new Token("");
    static final Token MULTI = new Token("#");
    static final Token SINGLE = new Token("+");
    final String name;

    protected Token(String s) {
        name = s;
    }

    protected String name() {
        return name;
    }

    protected boolean match(Token t) {
        if (MULTI.equals(t) || SINGLE.equals(t)) {
            return false;
        }

        if (MULTI.equals(this) || SINGLE.equals(this)) {
            return true;
        }

        return equals(t);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Token other = (Token) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
