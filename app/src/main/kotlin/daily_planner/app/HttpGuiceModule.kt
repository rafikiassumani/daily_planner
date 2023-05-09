package daily_planner.app

import com.google.inject.AbstractModule
import com.google.inject.Provides
import daily_planner.app.http.HealthHttpController

class HttpGuiceModule: AbstractModule() {

    @Provides
    fun provideHttpHealthController() : HealthHttpController {
        return HealthHttpController()
    }

}