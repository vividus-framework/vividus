package org.vividus.zephyr.facade;

import org.vividus.zephyr.model.ZephyrApiType;

public class ZephyrFacadeFactory
{
    private final ZephyrFacade zephyrFacade;
    private final ZephyrScaleFacade zephyrScaleFacade;
    private final ZephyrApiType zephyrApiType;

    public ZephyrFacadeFactory(ZephyrApiType zephyrApiType,
                               ZephyrFacade zephyrFacade,
                               ZephyrScaleFacade zephyrScaleFacade)
    {
        this.zephyrApiType = zephyrApiType;
        this.zephyrFacade = zephyrFacade;
        this.zephyrScaleFacade = zephyrScaleFacade;
    }

    public IZephyrFacade getZephyrFacade()
    {
        if (zephyrApiType == ZephyrApiType.SQUAD)
        {
            return zephyrFacade;
        }
        else
        {
            return zephyrScaleFacade;
        }
    }
}
