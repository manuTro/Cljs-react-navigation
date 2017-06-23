(ns navigation.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [navigation.events]
            [navigation.subs]))

(def ReactNative (js/require "react-native"))
(def ReactNav (js/require "react-navigation"))
(def cardstyle (js/require "react-navigation/src/views/CardStackStyleInterpolator.js"))
(def NavigationActions (.-NavigationActions ReactNav))
(defonce Text (.-Text ReactNative))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Stack-navigator (.-StackNavigator ReactNav))
(def TabNavigator (.-TabNavigator ReactNav))
(def DrawerNavigator (.-DrawerNavigator ReactNav))
(def StackRouter (.-StackRouter ReactNav))

;goback is like a pop for StackNavigator, but for TabNavigator Goes to the first tab, if not already selected

;; examples of functions to use with dispatch
(def navigateAction (.. NavigationActions (navigate #js {:routeName "Chat"}))) ;use with dispatch
(def goback (.. NavigationActions (back #js {:key "Chat"}))) ;use with dispatch
(def gotochat (.. NavigationActions (reset #js {:index 1
                                                :actions #js[(.. NavigationActions (navigate #js {:routeName "Chat"}))]})))
(def gotosec (.. NavigationActions (navigate #js {:routeName "Main" :action (.. NavigationActions (navigate #js {:routeName "Second"}))})))

(def routes (atom {}))

(defn my-screen []
  "In this screen we use the reagent current-component component function insted of this-as this"
           (let [nav (.. (r/current-component) -props -navigation)]
           (swap! routes assoc :my-screen nav)
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press #(.navigate nav "Second")
                                   ;#(.. nav (dispatch gotosec)) ;second way
}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Press Me"]]]))

(defn second-screen []
  "In this screen the navigation props is imported with this-as this"
  (this-as this
           (let [nav (.. this -props -navigation)]
                (swap! routes assoc :second nav)
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press
                                         #(.navigate (get @routes :main) "Chat")}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Second"]]])))

(defn chat-screen [{:keys [navigation]}]
  "In this screen the navigation props is passed as a key"
           (swap! routes assoc :chat nav)
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press  #(.navigate navigation "Prova")}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Go back"]]])


(defn prova-screen [{:keys [navigation]}]
             (swap! routes assoc :prova navigation)
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press #(.navigate navigation "Chat")}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Prova"]]])


  (defn wrapper
   "Convert the reagent component into a normal react component and
   Set the navigationOptions into the component."
   [screen title]
   (let [reactscreen (r/reactify-component screen)]
     (aset reactscreen "navigationOptions" (clj->js {:title title})) reactscreen))



(defn main-screen [{:keys [navigation]}]
    (swap! routes assoc :main navigation)
  (r/create-element (Stack-navigator (clj->js {:MyScreen {:screen MyScreen}
                                               :Second {:screen SecondScreen}})
                            ;use this options if you have 2 nested tabnavigator                    
                                   ;(clj->js {:swipeEnabled false :animationEnabled false
                                              ;:navigationOptions {:tabBarVisible false}})
                                  (clj->js  {:navigationOptions {:header null}
                                            :cardstyle {:backgroundColor "white"}
                                            :transitionConfig (fn [] #js {:screenInterpolator (.-forHorizontal (.-default cardstyle))})}) ;per lo stack per far muovere orizzontalmente
                                  ;(clj->js {:navigationOptions {:header null}})
                                  )))


(def MainScreen (wrapper main-screen "Main"))
(def MyScreen (wrapper my-screen "Welcome"))
(def ChatScreen (wrapper chat-screen "chat"))
(def SecondScreen (wrapper second-screen "Second"))
(def ProvaScreen (wrapper prova-screen "Prova"))

(defn app-root []
  "  With :tabBarVisible false I decided to hide the header
    If you use StackNavigator to hide the bar use :header null"
                          (r/create-element
                            (Stack-navigator
                             (clj->js {
                                       :Main {:screen MainScreen}
                                       :Chat {:screen ChatScreen}
                                       :Prova {:screen ProvaScreen}
                                       })
                                       ))
                                       )


(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "navigation" #(r/reactify-component app-root)))
