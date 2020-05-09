/*
 * Copyright (c) 2020, Daniel Sager <https://github.com/DanSager>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.vorkathhelper;

import net.runelite.api.NPC;
import net.runelite.api.ProjectileID;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

@PluginDescriptor(
        name = "Vorkath Helper",
        description = "Counts attacks since last special and displays next special attack",
        tags = {"vorkath"},
        loadWhenOutdated = true
)
public class VorkathHelperPlugin extends Plugin
{

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private SpriteManager spriteManager;

    private AttackCounter counterBox;

    NPC vorkath;
    int attackCount;
    private static final int ASLEEP_ID = 8059;
    private static final int AWAKE_ID = 8061;
    private static VorkathSpecial NEXT_SPECIAL;
    private static String LAST_PROJECTILE = "";

    @Override
    protected void shutDown() throws Exception
    {
        removeInfobox();
    }

    @Subscribe
    public void onNpcSpawned(final NpcSpawned event)
    {
        final int id = event.getNpc().getId();

        if (id == ASLEEP_ID)
            vorkath = event.getNpc();
    }

    @Subscribe
    public void onNpcChanged(final NpcChanged event)
    {
        if (AWAKE_ID == event.getNpc().getId())
        {
            // Vorkath awoke
            vorkath = event.getNpc();
            attackCount = 0;
            NEXT_SPECIAL = VorkathSpecial.UNKNOWN;
            updateInfobox();
        }
    }

    @Subscribe
    public void onGameObjectSpawned(final GameObjectSpawned event)
    {
        int id = event.getGameObject().getId();
        if (id == 32000)
        {
            //System.out.println("GAMEOBJECT: VORKATH_POISON_POOL_QUICKFIRE_BARRAGE");
            attackCount = 0;
            NEXT_SPECIAL = VorkathSpecial.ICEBARRAGE;
            updateInfobox();
        }
    }

    @Subscribe
    public void onGraphicsObjectCreated(final GraphicsObjectCreated event)
    {
        int id = event.getGraphicsObject().getId();
        if (id == 1466)
        {
            System.out.println("GRAPHICSOBJECT: VORKATH_HIGH_DAMAGE_DRAGONFIRE");
            attackCount++;
            updateInfobox();
        }
    }

    @Subscribe
    public void onNpcDespawned(final NpcDespawned event)
    {
        if (vorkath == event.getNpc())
        {
            vorkath = null;
            attackCount = 0;
            NEXT_SPECIAL = VorkathSpecial.UNKNOWN;
            removeInfobox();
        }
    }

    @Subscribe
    public void onProjectileMoved(final ProjectileMoved event)
    {
        int id = event.getProjectile().getId();
        String value = event.getProjectile().toString();

        if (LAST_PROJECTILE.equals(value))
            return;

        switch (id)
        {
            case ProjectileID.VORKATH_DRAGONBREATH:
                System.out.println("PROJECTILE: VORKATH_DRAGONBREATH");
                LAST_PROJECTILE = value;
                attackCount++;
                updateInfobox();
                break;
            case ProjectileID.VORKATH_MAGIC:
                System.out.println("PROJECTILE: VORKATH_MAGIC");
                LAST_PROJECTILE = value;
                attackCount++;
                updateInfobox();
                break;
            case ProjectileID.VORKATH_PRAYER_DISABLE:
                System.out.println("PROJECTILE: VORKATH_PRAYER_DISABLE");
                LAST_PROJECTILE = value;
                attackCount++;
                updateInfobox();
                break;
            case ProjectileID.VORKATH_RANGED:
                System.out.println("PROJECTILE: VORKATH_RANGED");
                LAST_PROJECTILE = value;
                attackCount++;
                updateInfobox();
                break;
            case ProjectileID.VORKATH_VENOM:
                System.out.println("PROJECTILE: VORKATH_VENOM");
                LAST_PROJECTILE = value;
                attackCount++;
                updateInfobox();
                break;
            case 395:
                System.out.println("PROJECTILE: VORKATH_FREEZE");
                LAST_PROJECTILE = value;
                attackCount = 0;
                NEXT_SPECIAL = VorkathSpecial.POISONPOOL;
                updateInfobox();
                break;
        }
    }

    private void updateInfobox()
    {
        attackCount = attackCount % 7;
        if (attackCount == 0)
        {
            removeInfobox();

            final BufferedImage image = spriteManager.getSprite(NEXT_SPECIAL.getSpriteID(), 0);
            counterBox = new AttackCounter(this, attackCount, image);
            infoBoxManager.addInfoBox(counterBox);
        }
        else if (counterBox != null)
        {
            counterBox.setCount(attackCount);
            return;
        }
    }

    private void removeInfobox()
    {
        infoBoxManager.removeInfoBox(counterBox);
        counterBox = null;
    }
}

class AttackCounter extends Counter
{
    AttackCounter(Plugin plugin, int count, BufferedImage image)
    {
        super(image, plugin, count);
    }
}
