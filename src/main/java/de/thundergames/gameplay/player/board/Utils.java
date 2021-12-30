/*
 * Copyright Notice for SwtPra10
 * Copyright (c) at ThunderGames | SwtPra10 2021
 * File created on 24.12.21, 10:56 by Carina Latest changes made by Carina on 23.12.21, 13:20
 * All contents of "Utils" are protected by copyright. The copyright law, unless expressly indicated otherwise, is
 * at ThunderGames | SwtPra10. All rights reserved
 * Any type of duplication, distribution, rental, sale, award,
 * Public accessibility or other use
 * requires the express written consent of ThunderGames | SwtPra10.
 */

package de.thundergames.playmechanics.board;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Utils {
  public static String getSprite(@NotNull final String spriteName) {
    return Objects.requireNonNull(Utils.class.getResource("/sprites/" + spriteName)).toString();
  }
}
