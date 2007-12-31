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

}
