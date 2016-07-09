package de.synyx.google.calendar.api.model;

import de.synyx.google.calendar.api.service.Query;

/**
 * @author clausen - clausen@synyx.de
 */
public interface Calendar {

    public String name ();

    public Query<Event> query ();

}
