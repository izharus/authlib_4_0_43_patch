package com.mojang.authlib.yggdrasil.request;

import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.List;

public class TelemetryEventsRequest {
   public final List<TelemetryEventsRequest.Event> events;

   public TelemetryEventsRequest(List<TelemetryEventsRequest.Event> events) {
      this.events = events;
   }

   public static class Event {
      public final String source;
      public final String name;
      public final long timestamp;
      public final JsonObject data;

      public Event(String source, String name, Instant timestamp, JsonObject data) {
         this.source = source;
         this.name = name;
         this.timestamp = timestamp.getEpochSecond();
         this.data = data;
      }
   }
}
