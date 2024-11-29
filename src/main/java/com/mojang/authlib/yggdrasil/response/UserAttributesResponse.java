package com.mojang.authlib.yggdrasil.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class UserAttributesResponse extends Response {
   @Nullable
   private UserAttributesResponse.Privileges privileges;
   @Nullable
   private UserAttributesResponse.ProfanityFilterPreferences profanityFilterPreferences;
   @Nullable
   private UserAttributesResponse.BanStatus banStatus;

   @Nullable
   public UserAttributesResponse.Privileges getPrivileges() {
      return this.privileges;
   }

   @Nullable
   public UserAttributesResponse.ProfanityFilterPreferences getProfanityFilterPreferences() {
      return this.profanityFilterPreferences;
   }

   @Nullable
   public UserAttributesResponse.BanStatus getBanStatus() {
      return this.banStatus;
   }

   public static class Privileges {
      @Nullable
      private UserAttributesResponse.Privileges.Privilege onlineChat;
      @Nullable
      private UserAttributesResponse.Privileges.Privilege multiplayerServer;
      @Nullable
      private UserAttributesResponse.Privileges.Privilege multiplayerRealms;
      @Nullable
      private UserAttributesResponse.Privileges.Privilege telemetry;
      @Nullable
      private UserAttributesResponse.Privileges.Privilege optionalTelemetry;

      public boolean getOnlineChat() {
         return this.onlineChat != null && this.onlineChat.enabled;
      }

      public boolean getMultiplayerServer() {
         return this.multiplayerServer != null && this.multiplayerServer.enabled;
      }

      public boolean getMultiplayerRealms() {
         return this.multiplayerRealms != null && this.multiplayerRealms.enabled;
      }

      public boolean getTelemetry() {
         return this.telemetry != null && this.telemetry.enabled;
      }

      public boolean getOptionalTelemetry() {
         return this.optionalTelemetry != null && this.optionalTelemetry.enabled;
      }

      public class Privilege {
         private boolean enabled;
      }
   }

   public static class ProfanityFilterPreferences {
      private boolean profanityFilterOn;

      public boolean isEnabled() {
         return this.profanityFilterOn;
      }
   }

   public static class BanStatus {
      private Map<String, UserAttributesResponse.BanStatus.BannedScope> bannedScopes;

      public Map<String, UserAttributesResponse.BanStatus.BannedScope> getBannedScopes() {
         return this.bannedScopes;
      }

      public static class BannedScope {
         private UUID banId;
         @Nullable
         private Instant expires;
         private String reason;
         @Nullable
         private String reasonMessage;

         public UUID getBanId() {
            return this.banId;
         }

         @Nullable
         public Instant getExpires() {
            return this.expires;
         }

         public String getReason() {
            return this.reason;
         }

         @Nullable
         public String getReasonMessage() {
            return this.reasonMessage;
         }
      }
   }
}
