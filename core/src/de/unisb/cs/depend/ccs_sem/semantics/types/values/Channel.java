package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;



/**
 * This interface is implements by all {@link Value}s that can represent a channel.
 *
 * @author Clemens Hammacher
 */
public interface Channel extends Value {

    /**
     * {@inheritDoc}
     *
     * (the instantiation of a Channel must again be a Channel)
     */
    public Channel instantiate(Map<Parameter, Value> parameters);

    /**
     * Compares this channel to another channel.
     *
     * @param other the other channel
     * @return <code>true</code> if the two Channel objects represent the same
     *         channel
     */
    public boolean sameChannel(Channel other);

}
