package de.unisb.cs.depend.ccs_sem.semantics.types;


public class OutputAction extends Action {

    private String firstPart;
    private String secondPart;

    public OutputAction(String firstPart, String secondPart) {
        super();
        this.firstPart = firstPart;
        this.secondPart = secondPart;
    }

    @Override
    public String getValue() {
        StringBuilder sb = new StringBuilder(firstPart.length() + secondPart.length() + 1);
        sb.append(firstPart).append('!').append(secondPart);
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((firstPart == null) ? 0 : firstPart.hashCode());
        result = PRIME * result + ((secondPart == null) ? 0 : secondPart.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OutputAction other = (OutputAction) obj;
        if (firstPart == null) {
            if (other.firstPart != null)
                return false;
        } else if (!firstPart.equals(other.firstPart))
            return false;
        if (secondPart == null) {
            if (other.secondPart != null)
                return false;
        } else if (!secondPart.equals(other.secondPart))
            return false;
        return true;
    }

}
