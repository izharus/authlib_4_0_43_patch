package com.mojang.authlib.yggdrasil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.authlib.Environment;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.authlib.yggdrasil.response.BlockListResponse;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.authlib.yggdrasil.response.UserAttributesResponse;
import java.net.Proxy;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

public class YggdrasilUserApiService implements UserApiService {
   private static final long BLOCKLIST_REQUEST_COOLDOWN_SECONDS = 120L;
   private static final UUID ZERO_UUID = new UUID(0L, 0L);
   private final URL routePrivileges;
   private final URL routeBlocklist;
   private final URL routeKeyPair;
   private final URL routeAbuseReport;
   private final MinecraftClient minecraftClient;
   private final Environment environment;
   private UserApiService.UserProperties properties;
   @Nullable
   private Instant nextAcceptableBlockRequest;
   @Nullable
   private Set<UUID> blockList;

   public YggdrasilUserApiService(String accessToken, Proxy proxy, Environment env) throws AuthenticationException {
      this.properties = OFFLINE_PROPERTIES;
      this.minecraftClient = new MinecraftClient(accessToken, proxy);
      this.environment = env;
      this.routePrivileges = HttpAuthenticationService.constantURL(env.getServicesHost() + "/player/attributes");
      this.routeBlocklist = HttpAuthenticationService.constantURL(env.getServicesHost() + "/privacy/blocklist");
      this.routeKeyPair = HttpAuthenticationService.constantURL(env.getServicesHost() + "/player/certificates");
      this.routeAbuseReport = HttpAuthenticationService.constantURL(env.getServicesHost() + "/player/report");
      this.fetchProperties();
   }

   public UserApiService.UserProperties properties() {
      return this.properties;
   }

   public TelemetrySession newTelemetrySession(Executor executor) {
      return (TelemetrySession)(!this.properties.flag(UserApiService.UserFlag.TELEMETRY_ENABLED) ? TelemetrySession.DISABLED : new YggdrassilTelemetrySession(this.minecraftClient, this.environment, executor));
   }

   public KeyPairResponse getKeyPair() {
      return null;
      //return (KeyPairResponse)this.minecraftClient.post(this.routeKeyPair, KeyPairResponse.class);
   }

   public boolean isBlockedPlayer(UUID playerID) {
      if (playerID.equals(ZERO_UUID)) {
         return false;
      } else {
         if (this.blockList == null) {
            this.blockList = this.fetchBlockList();
            if (this.blockList == null) {
               return false;
            }
         }

         return this.blockList.contains(playerID);
      }
   }

   public void refreshBlockList() {
      if (this.blockList == null || this.canMakeBlockListRequest()) {
         this.blockList = this.forceFetchBlockList();
      }

   }

   @Nullable
   private Set<UUID> fetchBlockList() {
      return !this.canMakeBlockListRequest() ? null : this.forceFetchBlockList();
   }

   private boolean canMakeBlockListRequest() {
      return this.nextAcceptableBlockRequest == null || Instant.now().isAfter(this.nextAcceptableBlockRequest);
   }

   private Set<UUID> forceFetchBlockList() {
      return null;
      /*
      this.nextAcceptableBlockRequest = Instant.now().plusSeconds(120L);

      try {
         BlockListResponse response = (BlockListResponse)this.minecraftClient.get(this.routeBlocklist, BlockListResponse.class);
         return response.getBlockedProfiles();
      } catch (MinecraftClientHttpException var2) {
         return null;
      } catch (MinecraftClientException var3) {
         return null;
      }

       */
   }

   private void fetchProperties() throws AuthenticationException {
      /*

      try {
         UserAttributesResponse response = (UserAttributesResponse)this.minecraftClient.get(this.routePrivileges, UserAttributesResponse.class);
         Builder<UserApiService.UserFlag> flags = ImmutableSet.builder();
         UserAttributesResponse.Privileges privileges = response.getPrivileges();
         if (privileges != null) {
            addFlagIfUserHasPrivilege(privileges.getOnlineChat(), UserApiService.UserFlag.CHAT_ALLOWED, flags);
            addFlagIfUserHasPrivilege(privileges.getMultiplayerServer(), UserApiService.UserFlag.SERVERS_ALLOWED, flags);
            addFlagIfUserHasPrivilege(privileges.getMultiplayerRealms(), UserApiService.UserFlag.REALMS_ALLOWED, flags);
            addFlagIfUserHasPrivilege(privileges.getTelemetry(), UserApiService.UserFlag.TELEMETRY_ENABLED, flags);
            addFlagIfUserHasPrivilege(privileges.getOptionalTelemetry(), UserApiService.UserFlag.OPTIONAL_TELEMETRY_AVAILABLE, flags);
         }

         UserAttributesResponse.ProfanityFilterPreferences profanityFilterPreferences = response.getProfanityFilterPreferences();
         if (profanityFilterPreferences != null && profanityFilterPreferences.isEnabled()) {
            flags.add(UserApiService.UserFlag.PROFANITY_FILTER_ENABLED);
         }

         Map<String, BanDetails> bannedScopes = new HashMap();
         if (response.getBanStatus() != null) {
            response.getBanStatus().getBannedScopes().forEach((scopeType, scope) -> {
               bannedScopes.put(scopeType, new BanDetails(scope.getBanId(), scope.getExpires(), scope.getReason(), scope.getReasonMessage()));
            });
         }

         this.properties = new UserApiService.UserProperties(flags.build(), bannedScopes);
      } catch (MinecraftClientHttpException var6) {
      } catch (MinecraftClientException var7) {
      }

       */

   }

   private static void addFlagIfUserHasPrivilege(boolean privilege, UserApiService.UserFlag value, Builder<UserApiService.UserFlag> output) {
      if (privilege) {
         output.add(value);
      }

   }

   public void reportAbuse(AbuseReportRequest request) {
      //this.minecraftClient.post(this.routeAbuseReport, request, Response.class);
   }

   public boolean canSendReports() {
      return true;
   }

   public AbuseReportLimits getAbuseReportLimits() {
      return AbuseReportLimits.DEFAULTS;
   }
}
