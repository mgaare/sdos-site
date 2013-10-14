(ns sdos-site.user
  (:require [clojure.string :as str]
            [sdos-site.settings :refer :all]
            [sdos-site.layout :as layout]
            [sdos-site.views.user :as view]
            [sdos-site.models.users :as users]
            [sdos-site.email :refer (send-email)]
            [clojure.tools.logging :refer (info)]))

(defn send-validation-email
  [server email validation]
  (let [validation-link (str "http://dos.sauerworld.org/user/validate/"
                             validation)
        body [:alternative
              {:type "text/plain"
               :content (str
                         "
Thanks for registering your account for Saurbraten: Day of Sobriety.
To validate your email address, please go here:" validation-link)}
              {:type "text/html"
               :content (str
                         "
<p>Thanks for registering your account for Saurbraten: Day of Sobriety.
To validate your email address, please click on:</p>

<p><a href=\"" validation-link "\">" validation-link "</a></p>")}]
        message {:to email
                 :from "Sauerbraten Day of Sobriety <noreply@dos.sauerworld.org>"
                 :subject "Site Registration Email Address Validation"
                 :body body}]
    (send-email server message)))

(defn error-strings
  [errors]
  (mapcat (fn [[k v]]
            (map #(str (name k) " " %) v))
          errors))

(defn wrap-validation
  [h error-view-fn & opts]
  (fn [{:keys [session] :as req}]
    (let [opts-map (apply hash-map opts)
          uri (:uri req)
          excepts (:except opts-map)
          exception? (when excepts
                       (not empty?
                            (-> (re-pattern (str/join "||" excepts))
                                (re-find uri))))
          validated (-> session :user :validated)]
      (if exception?
        (h req)
        (if validated
          (h req)
          (error-view-fn (str "<p>Your email address has not yet been validated.</p>"
                              "<p><a href=\"/user/validate/resend\">Click here "
                              "to resend validation email.</a></p>")))))))

(defn profile-page
  [req]
  (let [db (:db req)
        user (-> req :session :user)
        profile-snippet (view/user-profile user)]))

(defn login-page
  [req]
  (layout/app-page (get-settings req) (view/login-page)))

(defn do-login
  [req]
  (do
    (let [username (-> req :params :username)
          password (-> req :params :password)]
      (if-let [user (users/check-login (:db req) username password)]
        (assoc-in redirect-home [:session :user] user)
        (layout/app-page (get-settings req)
                         (view/login-page "Invalid username or password."))))))

(defn do-logout
  [req]
  {:session {:user nil}
   :status 302
   :headers {"Location" "/"}
   :body ""})

(defn registration-page
  [req]
  (layout/app-page (get-settings req)
                   (view/registration-page)))

(defn do-registration
  [req]
  (let [{:keys [username password password-confirm email]} (:params req)
        registration {:username username
                      :password password
                      :password-confirm password-confirm
                      :email email}
        db (:db req)
        server (:smtp-server req)
        validation ((users/make-registration-validator db) registration)]
    (if-not (empty? validation) ;; escapes first
      (layout/app-page (get-settings req)
                       (view/registration-page (assoc registration
                                                 :error
                                                 (error-strings validation))))
      (do
        (users/insert-user db registration)
        (let [newuser (users/get-by-username db username)]
          (if (send-validation-email server email (:validation_key newuser))
            (layout/app-page (get-settings req)
                             (view/registration-thanks))
            (let [error-msg
                  "There was a problem sending your account validation email.
                   Please try again later."]
              (layout/error-template (get-settings req)
                                     error-msg))))))))

(defn password-page
  [req]
  (layout/app-page (get-settings req)
                   (view/password-page)))

(defn do-password
  [req]
  (let [password (-> req :params :password)
        password-confirm (-> req :params :password-confirm)
        validation (users/password-validator (:params req))]
    (if-not (nil? validation)
      ;; validation error case
      (layout/app-page (get-settings req)
                       (view/password-page
                        {:error (error-strings validation)}))
      ;; validation ok case
      (let [db (:db req)
            user (-> req :session :user)]
        (if (users/update-password db user password)
          (layout/app-page (get-settings req)
                           (view/success-page))
          (let [error-msg
                "There was a problem setting your new password.
                 Please try again later."]
            (layout/error-template (get-settings req)
                                   error-msg)))))))

(defn resend-validation
  [req]
  (let [server (:smtp-server req)
        user (-> req :session :user)
        email (:email user)
        validation (:validation_key user)
        required [["Email server" email] ["user" user]
                  ["email address" email] ["validation key" validation]]]
    (if (and user email validation server)
      (if (send-validation-email server email)
        (layout/app-page (get-settings req)
                         (view/success-page (str "Validation email resent to "
                                                 email)))
        (layout/error-template (get-settings req)
                               "There was a problem sending your validation
                                email. Please try again later."))
      (let [missing-fields (->> required
                                (filter #(nil? (second %)))
                                (map first)
                                (str/join ", ")
                                (str "Error - missing fields: "))]
        (layout/error-template (get-settings req)
                               missing-fields)))))

(defn validate-email
  [req]
  (let [submitted-key (-> req :request-params :validation-key)
        db (:db req)]
    (if-let [user (users/get-by-validation-key db submitted-key)]
      (if (users/set-validated db user)
        (let [session-user (-> req :session :user)
              body (view/success-page (str "Email validated."))]
          (if (= (:id user) (:id session-user))
            (let [session (:session req)]
              {:session (assoc-in [:session :user :validated] true)
               :body body})
            body))
        (layout/error-template (get-settings req)
                               "Sorry, unable to validate email address at this
                                time."))
      (layout/error-template (get-settings req)
                             "Sorry, email validation key not found."))))

(defn authkey-page
  [req]
  "authkey page")

(defn do-authkey
  [req]
  "do authkey")

(defn signup-page
  [req]
  "signup page")

(defn do-signup
  [req]
  "do signup")

(defn show-signup
  [req]
  "show signup")

(defn do-edit-signup
  [req]
  "do edit signup")
