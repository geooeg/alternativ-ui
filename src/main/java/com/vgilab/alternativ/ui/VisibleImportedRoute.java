package com.vgilab.alternativ.ui;

/**
 *
 * @author smuellner
 */
public enum VisibleImportedRoute {

    ANALYSED_DATA(0),
    THIS_ROUTE(1),
    ALL_ROUTES(2);

    private int visibleRouteCode = 0;

    private VisibleImportedRoute(int code) {
        this.visibleRouteCode = code;
    }

    public int getVisibleRouteCode() {
        return visibleRouteCode;
    }
}
