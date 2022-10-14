/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.selenium.screenshot;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.vividus.util.Sleeper;

import pazone.ashot.PageDimensions;
import pazone.ashot.ShootingDecorator;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.Coords;
import pazone.ashot.util.InnerScript;

/**
 *  Modified for debug copy of {@link pazone.ashot.ViewportPastingDecorator}
 */
// CHECKSTYLE:OFF
public class DebuggingViewportPastingDecorator extends ShootingDecorator {
    public static final String PAGE_DIMENSIONS_JS = "js/page_dimensions.js";
    private static final String SCROLL_Y_TEMPLATE =
              "if ('scrollBehavior' in document.documentElement.style) {"
            + "    %1$s.scrollTo({"
            + "        \"top\": arguments[0],"
            + "        \"left\": 0,"
            + "        \"behavior\": \"instant\""
            + "    });"
            + "} else {"
            + "    %1$s.scrollTo(0, arguments[0]);"
            + "}"
            + "return [];";
    private static final long serialVersionUID = 4173686031614281540L;
    private static final String CURRENT_SCROLL = "current_scroll_";
    protected int scrollTimeout;
    private Coords shootingArea;
    private transient ScreenshotDebugger screenshotDebugger;
    private final String scrollVerticallyScript;

    public DebuggingViewportPastingDecorator(ShootingStrategy strategy) {
        this(strategy, "window");
    }

    protected DebuggingViewportPastingDecorator(ShootingStrategy strategy, String target) {
        super(strategy);
        scrollVerticallyScript = String.format(SCROLL_Y_TEMPLATE, target);
    }

    public DebuggingViewportPastingDecorator withScrollTimeout(int scrollTimeout) {
        this.scrollTimeout = scrollTimeout;
        return this;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd) {
        return getScreenshot(wd, null);
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd, Set<Coords> coordsSet) {
        JavascriptExecutor js = (JavascriptExecutor) wd;
        PageDimensions pageDimensions = getPageDimensions(wd);
        shootingArea = getShootingCoords(coordsSet, pageDimensions);
        int startY = getCurrentScrollY(js);

        BufferedImage finalImage = new BufferedImage(pageDimensions.getViewportWidth(), shootingArea.height,
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();

        int viewportHeight = pageDimensions.getViewportHeight();
        int scrollTimes = (int) Math.ceil(shootingArea.getHeight() / viewportHeight);
        for (int n = 0; n < scrollTimes; n++) {
            scrollVertically(js, shootingArea.y + viewportHeight * n);
            Sleeper.sleep(Duration.ofMillis(scrollTimeout));
            BufferedImage part = getShootingStrategy().getScreenshot(wd);
            int currentScrollY = getCurrentScrollY(js);
            debugScreenshot(CURRENT_SCROLL + currentScrollY + "_part_" + n, part);
            graphics.drawImage(part, 0, currentScrollY - shootingArea.y, null);
            debugScreenshot(CURRENT_SCROLL + currentScrollY, finalImage);
        }

        graphics.dispose();
        scrollVertically(js, startY);
        return finalImage;
    }

    private void debugScreenshot(String debugMessage, BufferedImage debugImage) {
        if (screenshotDebugger != null) {
            screenshotDebugger.debug(this.getClass(), debugMessage, debugImage);
        }
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet) {
        return shootingArea == null ? coordsSet : shiftCoords(coordsSet, shootingArea);
    }

    protected PageDimensions getPageDimensions(WebDriver driver) {
        Map<String, Number> pageDimensions = InnerScript.execute(PAGE_DIMENSIONS_JS, driver);
        return new PageDimensions(pageDimensions.get("pageHeight").intValue(),
                pageDimensions.get("viewportWidth").intValue(), pageDimensions.get("viewportHeight").intValue());
    }

    protected int getCurrentScrollY(JavascriptExecutor js) {
        return ((Number) js.executeScript("var scrY = window.scrollY;"
                + "if(scrY){return scrY;} else {return 0;}")).intValue();
    }

    protected void scrollVertically(JavascriptExecutor js, int scrollY) {
        js.executeScript(scrollVerticallyScript, scrollY);
    }

    protected String getScrollVerticallyScript()
    {
        return scrollVerticallyScript;
    }

    private Coords getShootingCoords(Set<Coords> coords, PageDimensions pageDimensions) {
        if (coords == null || coords.isEmpty()) {
            return new Coords(0, 0, pageDimensions.getViewportWidth(), pageDimensions.getPageHeight());
        }
        return extendShootingArea(Coords.unity(coords), pageDimensions);
    }

    private Set<Coords> shiftCoords(Set<Coords> coordsSet, Coords shootingArea) {
        Set<Coords> shiftedCoords = new HashSet<>();
        if (coordsSet != null) {
            for (Coords coords : coordsSet) {
                coords.y -= shootingArea.y;
                shiftedCoords.add(coords);
            }
        }
        return shiftedCoords;
    }

    private Coords extendShootingArea(Coords shootingCoords, PageDimensions pageDimensions) {
        int halfViewport = pageDimensions.getViewportHeight() / 2;
        shootingCoords.y = Math.max(shootingCoords.y - halfViewport / 2, 0);
        shootingCoords.height = Math.min(shootingCoords.height + halfViewport, pageDimensions.getPageHeight());
        return shootingCoords;
    }

    public ShootingDecorator withDebugger(ScreenshotDebugger screenshotDebugger)
    {
        this.screenshotDebugger = screenshotDebugger;
        return this;
    }
}
//CHECKSTYLE:ON
