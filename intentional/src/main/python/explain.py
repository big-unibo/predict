import argparse
import json
import matplotlib.pyplot as plt
import numpy as np
import os
import pandas as pd
import random
import time
from os import path
from sklearn.metrics import r2_score, mean_squared_error
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.feature_selection import RFE
from sklearn.feature_selection import RFECV
from scipy import signal

# SEED all random generators
seed = 4
random.seed(seed)
os.environ['PYTHONHASHSEED'] = str(seed)
np.random.seed(seed)

# ==============================================================================
# Chart variables
# ==============================================================================
titlesize = 16
subtitlesize = 14
labelsize = 14
axessize = 12
legendsize = 11
markersize = 5


def prop_to_df(model, component, property, prop=None):
    if prop is None:
        prop = []
    for k, v in property.items():  # iterate over the properties of the component
        prop.append([model, component, k, v])  # append them
    if len(prop) == 0:  # if no properties
        raise ValueError('Empty properties')
    return pd.DataFrame(prop, columns=["model", "component", "property", "value"])


def multiple_regression_fit(df, y_label='y', x_labels=['x']):
    X, y = df[x_labels], df[y_label]
    model = LinearRegression()
    if len(x_labels) == 1:
        features = x_labels
    else:
        # while len(x_labels) > 1:
        #     RFE(model, n_features_to_select=len(x_labels) - 1, step=1)
        rfe = RFECV(estimator=model, step=1, cv=3)
        rfe = rfe.fit(X, y)
        features = X.columns[rfe.support_]
        # print(rfe.ranking_)
    model = LinearRegression()
    model.fit(X[features], y)
    z = [model.intercept_] + list(model.coef_)
    z = [round(v, 2) for v in z]
    property = {
        'interest': model.score(X[features], y),
        'equation': '{}={}+{}'.format(y_label, '+'.join([str(v) + "$\cdot$" + str(k) for k, v in zip(features, z[1:])]), z[0]),
        'coeff': z
    }
    # Plot 3D chart
    # fig = plt.figure()
    # ax = plt.axes(projection='3d')
    # A = np.arange(X[features[0]].min(), X[features[0]].max(), 100)
    # B = np.arange(X[features[1]].min(), X[features[1]].max(), 100)
    # A, B = np.meshgrid(A, B)
    # C = B - A
    # ax.plot_surface(A, B, C, rstride=1, cstride=1, color='lightblue', alpha=.5, linewidth=0, antialiased=False)
    # ax.scatter(X[[features[0]]], X[[features[1]]], y)
    # ax.set_xlabel("discount") # features[0]
    # ax.set_ylabel("grossRevenue") # features[1]
    # ax.set_zlabel("netRevenue") # y_label
    # fig.savefig('example_{}.pdf'.format("Multireg"))
    # fig.savefig('example_{}.svg'.format("Multireg"))
    # fig.tight_layout()
    return df, prop_to_df('Multireg', y_label, property), None, None, None, None


def time_series_fit(df, y_label, x_labels, max_lag=365):
    prop = []
    y, l = df[y_label] - df[y_label].mean(), len(df[y_label])
    var, lags = np.sum(y ** 2), signal.correlation_lags(l, l, mode="full") # the possible lags
    for component in x_labels:
        x = df[component] - df[component].mean()
        cc = signal.correlate(y, x, mode="full", method="auto")
        norm_cc = cc / np.sqrt(np.sum(x ** 2) * var)
        abs_cc = np.abs(norm_cc)
        best_lag = lags[np.argmax(abs_cc)]
        property = {
            "interest": np.max(abs_cc),
            "lag": best_lag,
            "r2": r2_score(df[y_label], df[component].reindex(index=np.roll(df.index, best_lag)))
        }
        P = prop_to_df('CrossCorrelation', component, property, prop)
        # Plot the line chart
        # import matplotlib.dates as mdates
        # import datetime as dt
        # fig = plt.figure()
        # ax = plt.axes()
        # ax.plot(df["the_date"], df[component], label="unitCost")
        # ax.plot(df["the_date"], df[y_label], label="unitPrice")
        # ax.set_xlabel("Date")
        # ax.legend(loc=2)
        # plt.gca().xaxis.set_major_formatter(mdates.DateFormatter('%b'))
        # plt.gca().xaxis.set_major_locator(mdates.DayLocator(interval=30))
        # plt.gcf().autofmt_xdate()
        # fig.savefig('example_{}.pdf'.format("CrossCorrelation"))
        # fig.savefig('example_{}.svg'.format("CrossCorrelation"))
        # fig.tight_layout()
    return df, P, None, None, None, None


def fit(df, r=5, kpi='score', m_size=1, test_size=0.33, x_label='x', y_label='y', plt_all=False):
    def myprint(z, x='x'):
        c = str(round(z[0], 2))
        z = z[1:]
        ret = c + ('' if len(z) == 0 else ('$\cdot ' + x + '$' if len(z) == 1 else '$\cdot ' + x + '^' + str(len(z)) + '$') + ('' if myprint(z, x=x).startswith('-') else '+') + myprint(z, x=x))
        return ret

    r = np.max([np.min([r, int(len(df) / 10) + 1]), 2]) # apply the one-to-ten rule to choose the maximum degree
    models = {}
    if test_size is not None:
        train, test = train_test_split(df, test_size=test_size, random_state=seed)
        train = train.sort_values(by=x_label)
        test = test.sort_values(by=x_label)
    else:
        train = df
        test = df
    # create the figures
    fig, axs, fig2, axs2 = None, None, None, None
    if plt_all:
        fig, axs = plt.subplots(3, r, figsize=(4 * r, 3 * 3), sharey=True)
        fig2, axs2 = plt.subplots(1, 1, figsize=(4, 3))
    # arrays of errors
    error_x, error_y = [], []
    minscore, minf, mtitle, argmin = float('inf'), None, None, None
    colors = ['red', 'blue', 'green', 'orange']
    # iterate over some polynomial degrees
    for i in range(0, r):
        color = colors[i % len(colors)]
        # plot the raw data
        if plt_all:
            ax0, ax1, ax2 = (axs[0][i], axs[1][i], axs[2][i]) if r > 1 else (axs[0], axs[1], axs[2])
            if r > 0:
                ax0.scatter(df[x_label], df[y_label], s=m_size, c='black')
                ax1.scatter(train[x_label], train[y_label], s=m_size, c='black')
                ax2.scatter(test[x_label], test[y_label], s=m_size, c='black')
            else:
                ax0.scatter(df[x_label], df[y_label], s=m_size, c='black', label='Raw')
                ax1.scatter(train[x_label], train[y_label], s=m_size, c='black', label='Raw')
                ax2.scatter(test[x_label], test[y_label], s=m_size, c='black', label='Raw')
        # fit the model
        z = np.polyfit(train[x_label], train[y_label], i)
        f = np.poly1d(z)
        m = '{}={}'.format(y_label, myprint(list(z), x=x_label))
        if plt_all:
            # ... and plot it
            x = [x for x in range(int(train[x_label].min()), int(train[x_label].max()), 1)]
            ax1.plot(x, f(x), linewidth=2, label='p(' + str(i) + ')', c=color)
            # test it
            x = [x for x in range(int(test[x_label].min()), int(test[x_label].max()), 1)]
            ax2.plot(x, f(x), linewidth=2, label='p(' + str(i) + ')', c=color)
            # In statistics, the coefficient of determination, denoted R2 or r2 and
            # pronounced "R squared", is the proportion of the variation in the
            # dependent variable that is predictable from the independent variable.
        test_values = f(test[x_label])
        interest = r2_score(test[y_label], test_values)
        mse = mean_squared_error(test[y_label], test_values)
        models[m] = {
            'interest': interest,
            'mse': mse,
            'score': mse * len(test) / (len(test) - i - 1),
            'degree': i,
            'equation': m,
            'coeff': [round(v, 2) for v in z]
        }
        error_x = error_x + [i]
        error_y = error_y + [models[m][kpi]]
        if models[m][kpi] < minscore:
            argmin = m
            minscore = models[m][kpi]
        if plt_all:
            x = [x for x in range(int(df[x_label].min()), int(df[x_label].max()), 1)]
            ax0.plot(x, f(x), linewidth=2, label='p(' + str(i) + ')', c=color)
            ax0.set_ylabel(y_label, fontsize=labelsize)
            ax0.set_xlabel(x_label, fontsize=labelsize)
            ax0.grid(True)
            ax1.set_title('Train', fontsize=titlesize)
            ax2.set_title('Test', fontsize=titlesize)
            ax2.legend()

    if plt_all:
        axs2.plot(error_x, error_y)
        axs2.set_xticks(list(range(0, r)))
        axs2.set_ylabel('$error$', fontsize=labelsize)
        axs2.set_xlabel('$degree$', fontsize=labelsize)
        axs2.grid(True)
        fig.tight_layout()
        fig2.tight_layout()
        fig.savefig('example_{}.pdf'.format(x_label))
        fig2.savefig('error_{}.pdf'.format(x_label))

    return models[argmin], axs if not plt_all else axs[0], fig, axs2, fig2


def fit_all(X, measure, othermeasures, r=6, kpi='score', plt_all=False):
    prop = []
    for m in othermeasures:
        component, ax, fig, axe, fige = fit(X, r=r, kpi=kpi, x_label=m, y_label=measure, plt_all=plt_all)
        P = prop_to_df('Polyfit', m, component, prop)
    return X, P, ax, fig, axe, fige


def run(X, measure, measures, using, execution_id=-1):
    P = pd.DataFrame()
    stats = []
    for model in using:
        start = time.time()
        if model == "Polyfit":
            _, curP, _, _, _, _ = fit_all(X, measure, measures, plt_all=False)
        elif model == "CrossCorrelation":
            _, curP, _, _, _, _ = time_series_fit(X, measure, measures)
        elif model == "Multireg":
            _, curP, _, _, _, _ = multiple_regression_fit(X, measure, measures)
        else:
            raise ValueError("Unknown model: " + model)
        end_time = round((time.time() - start) * 1000)  # time is in ms
        P = pd.concat([P, curP], ignore_index=True)
        stats.append([execution_id, model, end_time])
    return P, stats


if __name__ == '__main__':
    ###############################################################################
    # PARAMETERS SETUP
    ###############################################################################
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", help="where to put the output", type=str)
    parser.add_argument("--file", help="the file name", type=str)
    parser.add_argument("--session_step", help="the session step", type=int)
    parser.add_argument("--cube", help="cube", type=str)
    parser.add_argument("--measure", help="measure to explain", type=str)
    parser.add_argument("--execution_id", help="execution id", type=str)
    parser.add_argument("--against", help="measures for comparison", nargs='?', const='', default='', type=str)
    parser.add_argument("--using", help="models for explanation", nargs='?', const='', default='', type=str)
    args = parser.parse_args()
    my_path = args.path.replace("\"", "")
    file = args.file
    measure = args.measure
    session_step = args.session_step
    execution_id = args.execution_id
    cube = args.cube.replace("__", " ")
    cube = json.loads(cube)
    against = "" if args.against == "" else args.against.split(",")
    using = "" if args.using == "" else args.using.split(",")

    ###############################################################################
    # APPLY MODELS
    ###############################################################################
    try:
        X = pd.read_csv(my_path + file + "_" + str(session_step) + ".csv", encoding='cp1252')
    except Error:
        X = pd.read_csv(my_path + file + "_" + str(session_step) + ".csv", encoding="utf-8")

    if len(X) == 0:
        raise ValueError('Empty data')

    X.columns = [x.lower() for x in X.columns]
    measures = against if len(against) > 0 else [x["MEA"].lower() for x in cube["MC"]]
    if measure in measures:
        measures.remove(measure)
    if len(measures) < 1:
        raise ValueError("Not enough measures: " + str(measures))
    using = ["Polyfit", "CrossCorrelation", "Multireg"] if len(using) == 0 else using
    P, stats = run(X, measure, measures, using, execution_id)
    P.to_csv(my_path + file + "_" + str(session_step) + "_property.csv", index=False)
    file_path = my_path + "/../explain_time_python.csv"
    pd \
        .DataFrame(stats, columns=["execution_id", "model", "time_model_python"]) \
        .to_csv(file_path, index=False, mode='a', header=not path.exists(file_path))
