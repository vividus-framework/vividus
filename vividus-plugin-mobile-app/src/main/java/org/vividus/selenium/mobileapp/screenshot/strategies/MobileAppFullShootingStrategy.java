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

package org.vividus.selenium.mobileapp.screenshot.strategies;

import static org.vividus.selenium.mobileapp.screenshot.util.CoordsUtils.scale;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import javax.inject.Named;

import org.ddogleg.struct.DogArray;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.mobileapp.model.SwipeDirection;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.screenshot.ScreenshotUtils;
import org.vividus.selenium.screenshot.strategies.ScreenshotShootingStrategy;
import org.vividus.ui.util.ImageUtils;
import org.vividus.util.Sleeper;

import boofcv.alg.template.TemplateMatching;
import boofcv.factory.template.FactoryTemplateMatching;
import boofcv.factory.template.TemplateScoreType;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.util.ImageTool;

@Named("FULL")
public class MobileAppFullShootingStrategy implements ShootingStrategy, ScreenshotShootingStrategy
{
    private static final long serialVersionUID = -1367316946809918634L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAppFullShootingStrategy.class);

    private static final Duration SWIPE_DURATION = Duration.ofSeconds(2);
    private static final int DEFAULT_SHIFT = 100;
    private static final int DIVIDER = 3;
    private static final double CUT_PERCENTAGE = 0.15;
    private static final String UNSUPPORTED_MESSAGE = "Element screenshot is not supported";
    private static final double MATCH_SCORE_THRESHOLD = 0.99d;

    private final transient TouchActions touchActions;
    private final transient MobileAppWebDriverManager genericWebDriverManager;
    private final transient MobileApplicationConfiguration mobileApplicationConfiguration;
    private final transient AttachmentPublisher attachmentPublisher;

    public MobileAppFullShootingStrategy(TouchActions touchActions,
            MobileAppWebDriverManager genericWebDriverManager,
            MobileApplicationConfiguration mobileApplicationConfiguration, AttachmentPublisher attachmentPublisher)
    {
        this.touchActions = touchActions;
        this.genericWebDriverManager = genericWebDriverManager;
        this.mobileApplicationConfiguration = mobileApplicationConfiguration;
        this.attachmentPublisher = attachmentPublisher;
    }

    @Override
    public ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy)
    {
        return this;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver webDriver)
    {
        try
        {
            touchActions.swipeUntilEdge(SwipeDirection.DOWN, SWIPE_DURATION);
            int height = genericWebDriverManager.getSize().getHeight();
            int cutSize = (int) (height * CUT_PERCENTAGE);
            return getScreenshot(webDriver, ImageArea.from(cutSize, height - cutSize * 2), DEFAULT_SHIFT);
        }
        catch (IOException thrown)
        {
            throw new UncheckedIOException(thrown);
        }
    }

    @Override
    public BufferedImage getScreenshot(WebDriver webDriver, Set<Coords> coords)
    {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet)
    {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    private BufferedImage getScreenshot(WebDriver webDriver, ImageArea targetArea, int scrollY) throws IOException
    {
        int dpr = (int) genericWebDriverManager.getDpr();
        ImageArea imageArea = ImageArea.scaled(targetArea, dpr);

        int swipeRate = targetArea.getScrollableHeight() / DIVIDER;
        int startY = scrollY + swipeRate * 2;
        int endY = scrollY + swipeRate;

        Runnable swipeUp = () ->
        {
            touchActions.performVerticalSwipe(startY, endY, SWIPE_DURATION);
            Sleeper.sleep(mobileApplicationConfiguration.getSwipeStabilizationDuration());
        };

        try
        {
            BufferedImage fullPageImage = takeFullPageScreenshot(swipeUp, webDriver, imageArea, swipeRate * dpr);
            touchActions.swipeUntilEdge(SwipeDirection.DOWN, SWIPE_DURATION);
            return fullPageImage;
        }
        catch (TemplateMissmatchException thrown)
        {
            touchActions.swipeUntilEdge(SwipeDirection.DOWN, SWIPE_DURATION);

            publishImage("targetImage", thrown.getImage());
            publishImage("template", thrown.getTemplate());
            throw new IllegalStateException(thrown);
        }
    }

    private BufferedImage takeFullPageScreenshot(Runnable swipe, WebDriver webDriver,
            ImageArea imageArea, int swipeRate) throws TemplateMissmatchException, IOException
    {
        BufferedImage viewport = takeViewportScreenshot(webDriver);
        BufferedImage output = cropImage(viewport, 0, imageArea.getBottomStartY());

        int screenSwipeLimit = mobileApplicationConfiguration.getSwipeLimit();
        BufferedImage previousFrame = null;

        for (int count = 0; count <= screenSwipeLimit; count++)
        {
            swipe.run();

            viewport = takeViewportScreenshot(webDriver);

            BufferedImage area = cropArea(viewport, imageArea);

            BufferedImage currentFrame = cropImage(area, area.getHeight() - swipeRate * 2 + DEFAULT_SHIFT,
                    swipeRate - DEFAULT_SHIFT);
            if (previousFrame != null && ImageTool.equalImage(currentFrame).matches(previousFrame))
            {
                BufferedImage bottomFrame = cropImage(viewport, imageArea.getBottomStartY(),
                        viewport.getHeight() - imageArea.getBottomStartY());
                return concatImages(List.of(output, bottomFrame));
            }

            previousFrame = currentFrame;

            output = concatByTemplate(output, area, currentFrame);

            if (count == screenSwipeLimit)
            {
                LOGGER.warn("Taking of full page screenshot is stopped due to exceeded swipe limit '{}'",
                        screenSwipeLimit);
            }
        }

        return output;
    }

    private BufferedImage cropArea(BufferedImage image, ImageArea area)
    {
        return cropImage(image, area.getTopIndent(), area.getScrollableHeight());
    }

    private void publishImage(String name, BufferedImage image) throws IOException
    {
        String imageName = name + ".png";
        attachmentPublisher.publishAttachment(ImageUtils.encodeAsPng(image), imageName);
    }

    private BufferedImage takeViewportScreenshot(WebDriver webDriver) throws IOException
    {
        return ScreenshotUtils.takeViewportScreenshot(webDriver);
    }

    private static BufferedImage concatImages(List<BufferedImage> images)
    {
        int width = asInts(images, BufferedImage::getWidth).max().getAsInt();
        int height = asInts(images, BufferedImage::getHeight).sum();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphic = output.createGraphics();
        graphic.setPaint(Color.RED);
        graphic.setColor(Color.BLUE);
        graphic.fillRect(0, 0, width, height);
        int drawPosition = 0;
        for (BufferedImage image : images)
        {
            graphic.drawImage(image, null, 0, drawPosition);
            drawPosition += image.getHeight();
        }
        graphic.dispose();
        return output;
    }

    private static BufferedImage concatByTemplate(BufferedImage first, BufferedImage second, BufferedImage template)
            throws TemplateMissmatchException
    {
        int firstY = getOverlapY(first, template);
        int secondY = getOverlapY(second, template);
        return concatImages(List.of(
                cropImage(first, 0, firstY),
                cropImage(second, secondY, second.getHeight() - secondY)));
    }

    private static BufferedImage cropImage(BufferedImage image, int fromY, int toY)
    {
        return image.getSubimage(0, fromY, image.getWidth(), toY);
    }

    private static int getOverlapY(BufferedImage image, BufferedImage template) throws TemplateMissmatchException
    {
        GrayF32 imageGray = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
        GrayF32 templateGray = ConvertBufferedImage.convertFromSingle(template, null, GrayF32.class);
        DogArray<Match> results = match(imageGray, templateGray);

        if (results.getTail().score < MATCH_SCORE_THRESHOLD)
        {
            throw new TemplateMissmatchException(image, template);
        }

        return results.get(results.getSize() - 1).getY();
    }

    private static DogArray<Match> match(GrayF32 image, GrayF32 template)
    {
        TemplateMatching<GrayF32> matcher = FactoryTemplateMatching.createMatcher(TemplateScoreType.NCC, GrayF32.class);
        matcher.setImage(image);
        matcher.setTemplate(template.subimage(0, 0, template.getWidth(), template.getHeight()), null, 1);
        matcher.process();
        return matcher.getResults();
    }

    private static IntStream asInts(List<BufferedImage> images, ToIntFunction<BufferedImage> function)
    {
        return images.stream().mapToInt(function);
    }

    public static class TemplateMissmatchException extends Exception
    {
        private static final long serialVersionUID = 4945579279267119038L;

        private final transient BufferedImage image;
        private final transient BufferedImage template;

        public TemplateMissmatchException(BufferedImage image, BufferedImage template)
        {
            super("Unable to match the template in the target image");
            this.image = image;
            this.template = template;
        }

        public BufferedImage getImage()
        {
            return image;
        }

        public BufferedImage getTemplate()
        {
            return template;
        }
    }

    private static final class ImageArea
    {
        private final int topIndent;
        private final int scrollableHeight;

        private ImageArea(int topIndent, int scrollableHeight)
        {
            this.topIndent = topIndent;
            this.scrollableHeight = scrollableHeight;
        }

        private static ImageArea from(int topIndent, int scrollableHeight)
        {
            return new ImageArea(topIndent, scrollableHeight);
        }

        private static ImageArea scaled(ImageArea imageArea, int dpr)
        {
            return new ImageArea(scale(imageArea.getTopIndent(), dpr), scale(imageArea.getScrollableHeight(), dpr));
        }

        private int getTopIndent()
        {
            return topIndent;
        }

        private int getBottomStartY()
        {
            return getTopIndent() + getScrollableHeight();
        }

        private int getScrollableHeight()
        {
            return scrollableHeight;
        }
    }
}
