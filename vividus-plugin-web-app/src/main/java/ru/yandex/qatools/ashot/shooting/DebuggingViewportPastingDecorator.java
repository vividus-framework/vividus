/*
 * Copyright 2019-2020 the original author or authors.
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

package ru.yandex.qatools.ashot.shooting;

import static ru.yandex.qatools.ashot.util.InnerScript.PAGE_HEIGHT_JS;
import static ru.yandex.qatools.ashot.util.InnerScript.VIEWPORT_HEIGHT_JS;
import static ru.yandex.qatools.ashot.util.InnerScript.VIEWPORT_WIDTH_JS;
import static ru.yandex.qatools.ashot.util.InnerScript.execute;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.screenshot.ScreenshotDebugger;
import org.vividus.util.Sleeper;

import ru.yandex.qatools.ashot.coordinates.Coords;

/**
 *  Modified for debug copy of {@link ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator}
 */
// CHECKSTYLE:OFF
public class DebuggingViewportPastingDecorator extends ShootingDecorator {
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
    protected int scrollTimeout = 0;
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
        int pageHeight = getFullHeight(wd);
        int pageWidth = getFullWidth(wd);
        int viewportHeight = getWindowHeight(wd);
        shootingArea = getShootingCoords(coordsSet, pageWidth, pageHeight, viewportHeight);
        int startY = getCurrentScrollY(js);

        BufferedImage finalImage = new BufferedImage(pageWidth, shootingArea.height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();

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

    public int getFullHeight(WebDriver driver) {
        return ((Number) execute(PAGE_HEIGHT_JS, driver)).intValue();
    }

    public int getFullWidth(WebDriver driver) {
        return ((Number) execute(VIEWPORT_WIDTH_JS, driver)).intValue();
    }

    public int getWindowHeight(WebDriver driver) {
        return ((Number) execute(VIEWPORT_HEIGHT_JS, driver)).intValue();
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

    private Coords getShootingCoords(Set<Coords> coords, int pageWidth, int pageHeight, int viewPortHeight) {
        if (coords == null || coords.isEmpty()) {
            return new Coords(0, 0, pageWidth, pageHeight);
        }
        return extendShootingArea(Coords.unity(coords), viewPortHeight, pageHeight);
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

    private Coords extendShootingArea(Coords shootingCoords, int viewportHeight, int pageHeight) {
        int halfViewport = viewportHeight / 2;
        shootingCoords.y = Math.max(shootingCoords.y - halfViewport / 2, 0);
        shootingCoords.height = Math.min(shootingCoords.height + halfViewport, pageHeight);
        return shootingCoords;
    }

    public ShootingDecorator withDebugger(ScreenshotDebugger screenshotDebugger)
    {
        this.screenshotDebugger = screenshotDebugger;
        return this;
    }
}
//CHECKSTYLE:ON
