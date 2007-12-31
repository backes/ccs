package de.unisb.cs.depend.ccs_sem.semantics.types;


public class SimpleAction extends Action {

    private String name;

    public SimpleAction(String name) {
        super();
        this.name = name;
    }

    @Override
    public String getValue() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }

}
