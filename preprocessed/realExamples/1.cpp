#include <algorithm>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <iomanip>
#include <iostream>
#include <map>
#include <queue>
#include <set>
#include <sstream>
#include <string>
#include <vector>
#include <list>
#include <cassert>
#include <queue>
#include <deque>

using namespace std;
template <class _T> inline _T sqr(const _T& x) { return x * x; }
template <class _T> inline _T ABS(const _T& x) { return (x<0)?-x:x;}
template <class _T> inline string tostr(const _T& a) { ostringstream os(""); os << a; return os.str(); }
template <class _T> inline istream& operator << (istream& is, const _T& a) { is.putback(a); return is; }
template <class _T> inline _T gcd(const _T &a, const _T &b) {
    _T t;

    while (!(b == 0)) {
        t = a % b;
        a = b;
        b = t;
    }

    return a;
}

typedef long double ld;


const ld PI = 3.1415926535897932384626433832795;
const ld EPS = 1e-11;


typedef unsigned uns;
typedef signed long long i64;
typedef unsigned long long u64;
typedef set < int > SI;
typedef vector < ld > VD;
typedef vector < int > VI;
typedef vector < bool > VB;
typedef vector < string > VS;
typedef pair < int, int > PII;
typedef map < string, int > MSI;
typedef map < string, void * > MSV;






static bool const _debug_ = false;






inline uns getUnsigned() {
    uns curr;
    scanf("%u", &curr);
    return curr;
}

inline void getUnsigned(uns &one, uns &two) {
    scanf("%u%u", &one, &two);
}

inline void getUnsigned(uns &one, uns &two, uns &three) {
    scanf("%u%u%u", &one, &two, &three);
}

inline int getInt() {
    int curr;
    scanf("%d", &curr);
    return curr;
}

inline void getInt(int &one, int &two) {
    scanf("%d%d", &one, &two);
}

inline void getInt(int &one, int &two, int &three) {
    scanf("%d%d%d", &one, &two, &three);
}

inline double getDouble() {
    double curr;
    scanf("%lf", &curr);
    return curr;
}

inline void getDouble(double &one, double &two) {
    scanf("%lf%lf", &one, &two);
}

inline void getDouble(double &one, double &two, double &three) {
    scanf("%lf%lf%lf", &one, &two, &three);
}

inline void FLUSH() {
    string dummy;
    getline(cin, dummy);
}

inline string getString() {
    string curr;
    cin >> curr;
    return curr;
}

inline string getLine() {
    string curr;
    getline(cin, curr);
    return curr;
}

inline void split(string const &in, VS &out, char delim = ' ') {
    size_t start = 0; size_t len = 0;
    size_t end = in.size() -1;
    size_t foundAt = in.find_first_of(delim, start);
    while (foundAt != string::npos) {
        len = (foundAt - start);
        out.push_back(in.substr(start, len));
        start = foundAt+1;
        foundAt = in.find_first_of(delim, start);
    }
    if (foundAt != end) {
        out.push_back(in.substr(start));
    }
}



int solveDeceitful(deque<double> &N, deque<double> &K) {
    int score = 0;
    while (!N.empty()) {
        if (N.front() > K.front()) {
            ++score;
            N.pop_front();
            K.pop_front();
        } else if (N.back() < K.back()) {
            N.pop_front();
            K.pop_back();
        } else {
            ++score;
            N.pop_back();
            K.pop_back();
        }
    }
    return score;
}

int solveRegular(deque<double> &N, deque<double> &K) {
    int score = 0;
    while (!N.empty()) {
        if (N.back() > K.back()) {
            ++score;
            N.pop_back();
            K.pop_front();
        } else {
            N.pop_back();
            K.pop_back();
        }
    }
    return score;
}



inline void foreachTest(uns testNum) {

    int N;
    cin >> N;
    deque<double> NAOMI1, NAOMI2;
    double curr;
    int n;
    for (n=0; n<N; ++n) {
        cin >> curr;
        NAOMI1.push_back(curr);
        NAOMI2.push_back(curr);
    }
    deque<double> KEN1, KEN2;
    for (n=0; n<N; ++n) {
        cin >> curr;
        KEN1.push_back(curr);
        KEN2.push_back(curr);
    }


    sort(NAOMI1.begin(), NAOMI1.end());
    sort(NAOMI2.begin(), NAOMI2.end());
    sort(KEN1.begin(), KEN1.end());
    sort(KEN2.begin(), KEN2.end());
    int ANS1 = solveDeceitful(NAOMI1, KEN1);
    int ANS2 = solveRegular(NAOMI2, KEN2);


    cout << "Case #" << testNum << ": ";
    cout << ANS1 << " " << ANS2;


    cout << endl;
}

int main() {



    cout << setiosflags(ios::fixed) << setprecision(10);

    uns T = getUnsigned();
    for (uns tt=1U; tt<=T; ++tt) {

        foreachTest(tt);
    }
    return 0;
}
