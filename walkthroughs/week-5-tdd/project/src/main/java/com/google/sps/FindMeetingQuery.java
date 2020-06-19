// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collections;
import java.util.Comparator;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    TimeRange possibleTimeRange;
    ArrayList<TimeRange> result = new ArrayList<TimeRange>();

    if (TimeRange.WHOLE_DAY.duration() >= request.getDuration()) possibleTimeRange = TimeRange.WHOLE_DAY;
    else return result;

    ArrayList<Event> eventsList = new ArrayList<Event>(events);
    Collections.sort(eventsList, ORDER_BY_START);

    // Loop through all scheduled events. If there is enough time
    // between possibleTimeRange.start() and the beginning of the event under consideration,
    // add that time span into results. At the end of each iteration, possibleTimeRange is
    // updated to start at the end of the event under consideration.
    // If the end of the event under consideration is past the end of possibleTimeRange,
    // possibleTimeRange is set to null and no more events are considered.
    // At the end of the loop, result contains a list of time ranges where it is possible
    // to schedule a meeting.
    for (Event event : eventsList) {
        if (possibleTimeRange.overlaps(event.getWhen())) {
            if (eventContainsMandatoryAttendee(event, request)) {
                possibleTimeRange = updateResults(event, possibleTimeRange, result, request.getDuration());
            }
        }
        if (possibleTimeRange == null) break;
    }
    if (possibleTimeRange != null) result.add(possibleTimeRange);
    // Return timeslots that maximize number of optional attendees that can attend
    return maximizeAttendance(result, request, events);
  }

  public boolean eventContainsMandatoryAttendee(Event event, MeetingRequest request) {
      for (String attendee : event.getAttendees()) {
          if (request.getAttendees().contains(attendee)) return true;
      }
      return false;
  }
  
  // Identifies how the given timerange needs to be modified given an overlapping event. Since events are processed
  // from earliest to latest start time, any segmented timerange that ends earlier than the start of the event is
  // guaranteed to not overlap with an unprocessed event so can be added to the result list. If the splitting/
  // shortening of the given timerange produces a timerange starting after the end of the event, the function will
  // return that timerange to be processed in the next iteration.
  public TimeRange updateResults(Event event, TimeRange timerange, ArrayList<TimeRange> result, long duration) {
      boolean eventStartsBefore = event.getWhen().start() <= timerange.start();
      boolean eventEndsBefore = event.getWhen().end() <= timerange.end();
      if (eventStartsBefore && eventEndsBefore) {
          // xxxx          ==>    xxxx
          //   -----       ==>        ---
          TimeRange newTimeRange = TimeRange.fromStartEnd(event.getWhen().end(), timerange.end(), false);
          if (newTimeRange.duration() >= duration) return newTimeRange;
      }
      else if (!eventStartsBefore && eventEndsBefore) {
          //     xxx       ==>      xxx
          //  ---------    ==>    --   ---
          TimeRange timeRangeBeforeEvent = TimeRange.fromStartEnd(timerange.start(), event.getWhen().start(), false);
          TimeRange timeRangeAfterEvent = TimeRange.fromStartEnd(event.getWhen().end(), timerange.end(), false);
          if (timeRangeBeforeEvent.duration() >= duration) result.add(timeRangeBeforeEvent);
          if (timeRangeAfterEvent.duration() >= duration) return timeRangeAfterEvent;
          
      }
      else if (!eventStartsBefore && !eventEndsBefore) {
          //     xxxx      ==>       xxxx
          // -----         ==>    ---
          TimeRange newTimeRange = TimeRange.fromStartEnd(timerange.start(), event.getWhen().start(), false);
          if (newTimeRange.duration() >= duration) result.add(newTimeRange); 
      }
      // Update does not create new possible timerange, return null
      return null;
  }

  // Finds timeranges that maximize attendance of requested optional attendees. Calls recursive function
  // addAttendee() to try all combinations of optional attendees.
  public Collection<TimeRange> maximizeAttendance(ArrayList<TimeRange> mandatoryAttendeeResults, 
    MeetingRequest request, Collection<Event> events) {
        ArrayList<String> optionalAttendees = new ArrayList<>(request.getOptionalAttendees());
        ArrayList<TimeRange> currentResults = (ArrayList) mandatoryAttendeeResults.clone();
        ArrayList<TimeRange> bestResults = (ArrayList) mandatoryAttendeeResults.clone();
        // Wrap in array so can pass by reference
        int[] bestNum = { 0 };
        addAttendee(0, events, request.getDuration(), optionalAttendees, currentResults, 0, bestResults, bestNum);
        Collections.sort(bestResults, TimeRange.ORDER_BY_START);
        return bestResults;
  }

  public void addAttendee(int idx, Collection<Event> events, long duration, ArrayList<String> optionalAttendees,
    ArrayList<TimeRange> currentResults, int currOptAttendees, ArrayList<TimeRange> bestResults, int[] bestOptAttendees) {
        // reaches end of optional attendee list
        if (idx >= optionalAttendees.size()) {
            return;
        }
        ArrayList<TimeRange> prevResults = (ArrayList) currentResults.clone();
        for (Event event : events) {
            ListIterator<TimeRange> it = currentResults.listIterator();
            while (it.hasNext()) {
                TimeRange timerange = it.next();
                if (timerange.overlaps(event.getWhen())) {
                    if (event.getAttendees().contains(optionalAttendees.get(idx))) {
                        updateOptionalResults(event, timerange, it, duration);
                    }
                }
            }
        }
        currOptAttendees++;
        if (!currentResults.isEmpty()) {  
            if (currOptAttendees > bestOptAttendees[0]) {
                bestOptAttendees[0] = currOptAttendees;
                updateBestResults(bestResults, currentResults);
            }
            // try adding next optional attendee including current attendee
            addAttendee(idx+1, events, duration, optionalAttendees, currentResults, currOptAttendees, bestResults, bestOptAttendees);
        }
        // try adding next optional attendee without including current attendee
        addAttendee(idx+1, events, duration, optionalAttendees, prevResults, currOptAttendees-1, bestResults, bestOptAttendees);
  }

  public void updateOptionalResults(Event event, TimeRange timerange, ListIterator<TimeRange> it, long duration) {
      it.remove();
      // event starts before or at same time as timerange gives value <= 0
      int eventStartsBefore = TimeRange.ORDER_BY_START.compare(event.getWhen(), timerange);
      int eventEndsBefore = TimeRange.ORDER_BY_END.compare(event.getWhen(), timerange);
      if (eventStartsBefore <= 0 && eventEndsBefore <= 0) {
          // xxxx          ==>    xxxx
          //   -----       ==>        ---
          TimeRange newTimeRange = TimeRange.fromStartEnd(event.getWhen().end(), timerange.end(), false);
          if (newTimeRange.duration() >= duration) it.add(newTimeRange);
      }
      else if (eventStartsBefore > 0 && eventEndsBefore <= 0) {
          //     xxx       ==>      xxx
          //  ---------    ==>    --   ---
          TimeRange newTimeRange1 = TimeRange.fromStartEnd(timerange.start(), event.getWhen().start(), false);
          TimeRange newTimeRange2 = TimeRange.fromStartEnd(event.getWhen().end(), timerange.end(), false);
          if (newTimeRange1.duration() >= duration) it.add(newTimeRange1);
          if (newTimeRange2.duration() >= duration) it.add(newTimeRange2);
      }
      else if (eventStartsBefore > 0 && eventEndsBefore > 0) {
          //     xxxx      ==>       xxxx
          // -----         ==>    ---
          TimeRange newTimeRange = TimeRange.fromStartEnd(timerange.start(), event.getWhen().start(), false);
          if (newTimeRange.duration() >= duration) it.add(newTimeRange); 
      }
  }

  public void updateBestResults(ArrayList<TimeRange> bestResults, ArrayList<TimeRange> currentResults) {
      bestResults.clear();
	  for (TimeRange tr: currentResults) { bestResults.add(tr); }
  }

  /**
   * A comparator for sorting events by their start time in ascending order.
   * Should go in Event class but not sure I'm allowed to edit that.
   */
  public static final Comparator<Event> ORDER_BY_START = new Comparator<Event>() {
    @Override
    public int compare(Event a, Event b) {
      return Long.compare(a.getWhen().start(), b.getWhen().start());
    }
  };

}
