package com.mojang.authlib.yggdrasil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.request.JoinMinecraftServerRequest;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.util.UUIDTypeAdapter;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
   private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilMinecraftSessionService.class);
   private final URL joinUrl = HttpAuthenticationService.constantURL("http://127.0.0.1:8000/auth/join.php");
   private final URL hasJoinedUrl = HttpAuthenticationService.constantURL("http://127.0.0.1:8000/auth/hasJoined.php");
   private final URL profileUrl = HttpAuthenticationService.constantURL("http://127.0.0.1:8000/auth/profile.php?uuid=");
   private final Gson gson = (new GsonBuilder()).registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
   private final LoadingCache<GameProfile, GameProfile> insecureProfiles;

   protected YggdrasilMinecraftSessionService(YggdrasilAuthenticationService service, Environment env) {
      super(service);
      this.insecureProfiles = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build(new CacheLoader<GameProfile, GameProfile>() {
         public GameProfile load(GameProfile key) {
            return YggdrasilMinecraftSessionService.this.fillGameProfile(key, false);
         }
      });
      LOGGER.info("SkinFix by TaoGunner");
   }

   public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException {
      JoinMinecraftServerRequest request = new JoinMinecraftServerRequest();
      request.accessToken = authenticationToken;
      request.selectedProfile = profile.getId();
      request.serverId = serverId;
      this.getAuthenticationService().makeRequest(this.joinUrl, request, Response.class);
   }

   public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
      Map<String, Object> arguments = new HashMap();
      arguments.put("username", user.getName());
      arguments.put("serverId", serverId);
      if (address != null) {
         arguments.put("ip", address.getHostAddress());
      }

      URL url = HttpAuthenticationService.concatenateURL(this.hasJoinedUrl, HttpAuthenticationService.buildQuery(arguments));

      try {
         HasJoinedMinecraftServerResponse response = (HasJoinedMinecraftServerResponse)this.getAuthenticationService().makeRequest(url, (Object)null, HasJoinedMinecraftServerResponse.class);
         if (response != null && response.getId() != null) {
            GameProfile result = new GameProfile(response.getId(), user.getName());
            if (response.getProperties() != null) {
               result.getProperties().putAll(response.getProperties());
            }

            return result;
         } else {
            return null;
         }
      } catch (AuthenticationUnavailableException var8) {
         throw var8;
      } catch (AuthenticationException var9) {
         return null;
      }
   }

   public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) throws InsecurePublicKeyException {
      Property textureProperty = (Property)Iterables.getFirst(profile.getProperties().get("textures"), (Object)null);
      if (textureProperty == null) {
         return new HashMap();
      } else {
         String value = requireSecure ? this.getSecurePropertyValue(textureProperty) : textureProperty.getValue();

         MinecraftTexturesPayload result;
         try {
            String json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
            result = (MinecraftTexturesPayload)this.gson.fromJson(json, MinecraftTexturesPayload.class);
         } catch (JsonParseException var7) {
            LOGGER.error("Could not decode textures payload", var7);
            return new HashMap();
         }

         return (Map)(result != null && result.getTextures() != null ? result.getTextures() : new HashMap());
      }
   }

   public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
      if (profile.getId() == null) {
         return profile;
      } else {
         return !requireSecure ? (GameProfile)this.insecureProfiles.getUnchecked(profile) : this.fillGameProfile(profile, true);
      }
   }

   public String getSecurePropertyValue(Property property) throws InsecurePublicKeyException {
      return property.getValue();
   }

   protected GameProfile fillGameProfile(GameProfile profile, boolean requireSecure) {
      try {
         URL var10000 = this.profileUrl;
         URL url = HttpAuthenticationService.constantURL(var10000 + UUIDTypeAdapter.fromUUID(profile.getId()));
         url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);
         MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse)this.getAuthenticationService().makeRequest(url, (Object)null, MinecraftProfilePropertiesResponse.class);
         if (response == null) {
            LOGGER.debug("Couldn't fetch profile properties for {} as the profile does not exist", profile);
            return profile;
         } else {
            GameProfile result = new GameProfile(response.getId(), response.getName());
            result.getProperties().putAll(response.getProperties());
            profile.getProperties().putAll(response.getProperties());
            LOGGER.debug("Successfully fetched profile properties for {}", result);
            return result;
         }
      } catch (IllegalArgumentException | AuthenticationException var6) {
         LOGGER.warn("Couldn't look up profile properties for {}", profile, var6);
         return profile;
      }
   }

   public YggdrasilAuthenticationService getAuthenticationService() {
      return (YggdrasilAuthenticationService)super.getAuthenticationService();
   }
}
