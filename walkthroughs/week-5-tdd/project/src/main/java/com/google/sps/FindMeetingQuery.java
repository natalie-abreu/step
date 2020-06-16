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

public final class FindMeetingQuery {

// // MANDATORY ONLY
//   public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
//     ArrayList<TimeRange> result = new ArrayList<TimeRange>();
//     // if request duration exceeds a day, return empty list
//     if (TimeRange.WHOLE_DAY.duration() >= request.getDuration()) result.add(TimeRange.WHOLE_DAY);
//     else return result;
//     for (Event event : events) {
//         ListIterator<TimeRange> it = result.listIterator();
//         while (it.hasNext()) {
//             TimeRange timerange = it.next();
//             if (timerange.overlaps(event.getWhen())) {
//                 // checks if overlap in attendees
//                 for (String attendee : event.getAttendees()) {
//                     if (request.getAttendees().contains(attendee)) {
//                         // current timerange needs to be split or shortened
//                         it.remove();
//                         // event starts before or at same time as timerange gives value <= 0
//                        int eventStartsBefore = TimeRange.ORDER_BY_START.compare(event.getWhen(), timerange);
//                        int eventEndsBefore = TimeRange.ORDER_BY_END.compare(event.getWhen(), timerange);
//                         if (eventStartsBefore <= 0 && eventEndsBefore <= 0) {
//                             // xxxx          ==>    xxxx
//                             //   -----       ==>        ---
//                             TimeRange newTimeRange = TimeRange.fromStartEnd(event.getWhen().end(), timerange.end(), false);
//                             if (newTimeRange.duration() >= request.getDuration()) it.add(newTimeRange);
//                         }
//                         else if (eventStartsBefore > 0 && eventEndsBefore <= 0) {
//                             //     xxx       ==>      xxx
//                             //  ---------    ==>    --   ---
//                             TimeRange newTimeRange1 = TimeRange.fromStartEnd(timerange.start(), event.getWhen().start(), false);
//                             TimeRange newTimeRange2 = TimeRange.fromStartEnd(event.getWhen().end(), timerange.end(), false);
//                             if (newTimeRange1.duration() >= request.getDuration()) it.add(newTimeRange1);
//                             if (newTimeRange2.duration() >= request.getDuration()) it.add(newTimeRange2);
//                         }
//                         else if (eventStartsBefore > 0 && eventEndsBefore > 0) {
//                             //     xxxx      ==>       xxxx
//                             // -----         ==>    ---
//                             TimeRange newTimeRange = TimeRange.fromStartEnd(timerange.start(), event.getWhen().start(), false);
//                             if (newTimeRange.duration() >= request.getDuration()) it.add(newTimeRange); 
//                         }
//                         break;
//                     }
//                 }
//             }
//         }
//     }
//     return result;
//   }

// // MANDATORY + ALL/NO OPTIONAL

//   public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
//     ArrayList<TimeRange> result = new ArrayList<TimeRange>();
//     // if request duration exceeds a day, return empty list
//     if (TimeRange.WHOLE_DAY.duration() >= request.getDuration()) result.add(TimeRange.WHOLE_DAY);
//     else return result;
//     result = queryHelper(events, request.getAttendees(), request.getDuration(), result);
//     ArrayList<TimeRange> resultWithOptional = queryHelper(events, request.getOptionalAttendees(), request.getDuration(), result);
//     if (resultWithOptional.isEmpty()) return result;
//     else return resultWithOptional;
//   }
//   public ArrayList<TimeRange> queryHelper(Collection<Event> events, Collection<String> attendees, long duration, ArrayList<TimeRange> currentTimeRanges) {
//       ArrayList<TimeRange> result = (ArrayList) currentTimeRanges.clone();
//       for (Event event : events) {
//         ListIterator<TimeRange> it = result.listIterator();
//         while (it.hasNext()) {
//             TimeRange timerange = it.next();
//             if (timerange.overlaps(event.getWhen())) {
//                 // checks if overlap in attendees
//                 for (String attendee : event.getAttendees()) {
//                     if (attendees.contains(attendee)) {
//                         // current timerange needs to be split or shortened
//                         updateResults(event, timerange, it, duration);
//                         break;
//                     }
//                 }
//             }
//         }
//     }
//     return result;
//   }

// MANDATORY + MAX OPTIONAL
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      ArrayList<TimeRange> result = new ArrayList<TimeRange>();
    // if request duration exceeds a day, return empty list
    if (TimeRange.WHOLE_DAY.duration() >= request.getDuration()) result.add(TimeRange.WHOLE_DAY);
    else return result;
    // first find time slots that fulfill mandatory attendance
    for (Event event : events) {
        ListIterator<TimeRange> it = result.listIterator();
        while (it.hasNext()) {
            TimeRange timerange = it.next();
            if (timerange.overlaps(event.getWhen())) {
                // checks if overlap in attendees
                for (String attendee : event.getAttendees()) {
                    if (request.getAttendees().contains(attendee)) {
                        updateResults(event, timerange, it, request.getDuration());
                        break;
                    }
                }
            }
        }
    }
    // return timeslots that maximize number of optional attendees that can attend
    return maximizeAttendance(result, request, events);
  }
  

  public void updateResults(Event event, TimeRange timerange, ListIterator<TimeRange> it, long duration) {
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

  public Collection<TimeRange> maximizeAttendance(ArrayList<TimeRange> mandatoryAttendeeResults, 
    MeetingRequest request, Collection<Event> events) {
        ArrayList<String> optionalAttendees = new ArrayList<>(request.getOptionalAttendees());
        ArrayList<TimeRange> currentResults = (ArrayList) mandatoryAttendeeResults.clone();
        ArrayList<TimeRange> bestResults = (ArrayList) mandatoryAttendeeResults.clone();
        // wrap in array so can pass by reference
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
        // save non-updated results before changing list
        ArrayList<TimeRange> prevResults = (ArrayList) currentResults.clone();
        for (Event event : events) {
            ListIterator<TimeRange> it = currentResults.listIterator();
            while (it.hasNext()) {
                TimeRange timerange = it.next();
                if (timerange.overlaps(event.getWhen())) {
                    // checks if overlap in attendees
                    if (event.getAttendees().contains(optionalAttendees.get(idx))) {
                        updateResults(event, timerange, it, duration);
                    }
                }
            }
        }
        currOptAttendees++;
        if (!currentResults.isEmpty()) {  
            // check if current solution exceeds best
            if (currOptAttendees > bestOptAttendees[0]) {
                bestOptAttendees[0] = currOptAttendees;
                // update best results list in place
                updateBestResults(bestResults, currentResults);
            }
            // try adding next optional attendee including current attendee
            addAttendee(idx+1, events, duration, optionalAttendees, currentResults, currOptAttendees, bestResults, bestOptAttendees);
        }
        // try adding next optional attendee without including current attendee
        addAttendee(idx+1, events, duration, optionalAttendees, prevResults, currOptAttendees-1, bestResults, bestOptAttendees);
  }

  public void updateBestResults(ArrayList<TimeRange> bestResults, ArrayList<TimeRange> currentResults) {
      for (TimeRange t : currentResults) {
          if (!bestResults.contains(t)) {
              bestResults.add(t);
          }
      }
      bestResults.removeIf(t -> !currentResults.contains(t));
  }

}
