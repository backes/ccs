package de.unisb.cs.depend.ccs_sem.semantics.types;


public class ParameterOrProcessEqualsWrapper {

    private final Parameter param;
    private final ProcessVariable proc;

    public ParameterOrProcessEqualsWrapper(Parameter param) {
        assert param != null;
        this.param = param;
        this.proc = null;
    }

    public ParameterOrProcessEqualsWrapper(ProcessVariable proc) {
        assert proc != null;
        this.param = null;
        this.proc = proc;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(param == null ? proc : param);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ParameterOrProcessEqualsWrapper other = (ParameterOrProcessEqualsWrapper) obj;
        if (param != other.param)
            return false;
        if (proc != other.proc)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return param == null ? proc.toString() : param.toString();
    }
}
