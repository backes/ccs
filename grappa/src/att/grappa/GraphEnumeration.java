package att.grappa;

import java.util.Enumeration;

/**
 * An extension of the Enumeration interface specific to enumerations of
 * graph elements.
 *
 * @version 1.2, 10 Oct 2006; Copyright 1996 - 2006 by AT&T Corp.
 * @author  <a href="mailto:john@research.att.com">John Mocenigo</a>, <a href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public interface GraphEnumeration extends Enumeration<Element>
{
  /**
   * Get the root of this enumeration.
   *
   * @return the root subgraph for this enumeration
   */
  public Subgraph getSubgraphRoot();

  /**
   * Get the types of elements possibly contained in this enumeration.
   *
   * @return an indication of the types of elements in this enumeration
   * @see GrappaConstants#NODE
   * @see GrappaConstants#EDGE
   * @see GrappaConstants#SUBGRAPH
   */
  public int getEnumerationTypes();

  /**
   * A convenience method that should just return a cast
   * of a call to nextElement()
   *
   * @return the next graph element in the enumeration
   * @exception java.util.NoSuchElementException whenever the enumeration has no more
   *                                   elements.
   */
  public Element nextGraphElement() throws java.util.NoSuchElementException;
}
