/**
 * 
 */
package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

final class IdentityRule implements ISchedulingRule {

    public boolean isConflicting(ISchedulingRule rule) {
        return rule == this;
    }

    public boolean contains(ISchedulingRule rule) {
        return rule == this;
    }
}