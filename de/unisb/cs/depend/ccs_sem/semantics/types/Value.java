package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


public interface Value {
    String getValue();

    Value replaceParameters(List<Value> parameters);
}
