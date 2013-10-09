(ns sdos-site.settings)

(def base-url
  "http://dos.sauerworld.org")

(def links
  [["Home" "/"]
   ["About" "/about"]
   ["Events" "/events"]
   ;;["Results" "/results"]
   ])

(def user-links
  [["Sign Up For SDoS" "/user/signup"]
   ["Profile" "/user/"]
   ["Logout" "/user/logout"]
   ["Generate Auth Key" "/user/authkey"]])

(def admin-links
  [])

(def layout-settings
  {:email-title "Interested?"
   :email-subtitle "Sign up for email updates!"
   :menu-title "Menu"
   :menu-items links
   })


(defn get-settings [req]
  (let [logged-in? (not (nil? (-> req :session :user)))
        admin? (true? (-> req :session :user :admin))]
    (merge layout-settings {:logged-in logged-in?
                            :admin admin?})))
