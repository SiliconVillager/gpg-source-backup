//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_GETSESSION_H
#define WEB_FACEBOOK_GETSESSION_H

#include "FacebookServiceStates.h"

#include <QString>

//==============================================================================
// Forward Declarations
//==============================================================================

class QDomDocument;

//==============================================================================
// Declarations
//==============================================================================

/** Token used to identify an authentication request. */
typedef QString FacebookAuthenticationToken;

/** Session identifiers. */
struct FacebookGetSessionResult
{
  QString sessionKey;                         ///< Session key.
  QString sessionSecret;                      ///< Session secret for signature.
  QString uid;                                ///< Authenticated user.
};

/** Request operator. */
struct FacebookGetSessionRequest
{
  typedef FacebookAuthenticationToken Input;  ///< Input type.
  enum { requiresSession = false };           ///< Session required flag.
  const static QString requestName;           ///< Request name.

  /**
   * Confirms whether a user has successfully authenticated a session.
   * @param input input parameters.
   * @param parameters parameters sent to the server.
   * @return status.
   */
  FacebookServiceState operator()(const Input& input, QVector<QString>& parameters);
};

/** Response operator. */
struct FacebookGetSessionResponse
{
  typedef FacebookGetSessionResult Output;    ///< Output type.

  /**
   * Parses response for get session request.
   * @param document XML document that contains the response.
   * @param output parsed output.
   * @return status.
   */
  FacebookServiceState operator()(const QDomDocument& document, Output& output);
};

#endif // WEB_FACEBOOK_GETSESSION_H
