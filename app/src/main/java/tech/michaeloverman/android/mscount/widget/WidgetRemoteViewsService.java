package tech.michaeloverman.android.mscount.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Michael on 4/14/2017.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {

    public WidgetRemoteViewsService() {
        super();
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
