package com.mojang.authlib.yggdrasil;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface ServicesKeySet {
   ServicesKeySet EMPTY = (type) -> {
      return List.of();
   };

   static ServicesKeySet lazy(Supplier<ServicesKeySet> supplier) {
      return (type) -> {
         return ((ServicesKeySet)supplier.get()).keys(type);
      };
   }

   Collection<ServicesKeyInfo> keys(ServicesKeyType var1);
}
